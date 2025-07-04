package eu.invouk.projectnuclear.tile;

import eu.invouk.projectnuclear.Projectnuclear;
import eu.invouk.projectnuclear.blocks.CoalGenerator;
import eu.invouk.projectnuclear.energynet.EnergyNet;
import eu.invouk.projectnuclear.energynet.EnergyNetManager;
import eu.invouk.projectnuclear.energynet.IEnergyProducer;
import eu.invouk.projectnuclear.gui.menu.CoalGeneratorMenu;
import eu.invouk.projectnuclear.models.IOverlayRenderable;
import eu.invouk.projectnuclear.models.MachineRenderer;
import eu.invouk.projectnuclear.register.ModBlocksEntities;
import eu.invouk.projectnuclear.utils.BurnableItemStackHandler;
import eu.invouk.projectnuclear.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CoalGeneratorTile extends BlockEntity implements MenuProvider, IOverlayRenderable, IEnergyProducer {

    private EnergyNet net;
    private final int voltage = 32; // LV

    private int burnTime = 0;       // Koľko tickov ešte horí palivo
    private int maxBurnTime = 0;
    private final int energyPerTick = 97; // Koľko energie vyrobí za jeden tick

    private final MachineRenderer machineRenderer;
    private boolean active;

    private final SimpleContainerData containerData = new SimpleContainerData(3);
    private final EnergyStorage energyStorage = new EnergyStorage(1000000, 128) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (!simulate && received > 0) {
                onEnergyChanged();
            }
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = super.extractEnergy(maxExtract, simulate);
            if (!simulate && extracted > 0) {
                onEnergyChanged();
            }
            return extracted;
        }
    };

    private void onEnergyChanged() {
        if (level != null && !level.isClientSide()) {
            setChanged();
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    private final BurnableItemStackHandler itemHandler = new BurnableItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            setChanged();
        }
    };

    public CoalGeneratorTile(BlockPos blockPos, BlockState blockState) {
        super(ModBlocksEntities.COAL_GENERATOR_TILE.get(), blockPos, blockState);
        machineRenderer = new MachineRenderer();
    }

    @Override
    public BlockPos getPosition() {
        return this.worldPosition;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CoalGeneratorTile blockEntity) {
        if(level.isClientSide) return;

        boolean dirty = false;

        if(blockEntity.getEnergyStorage().getEnergyStored() >= blockEntity.getEnergyStorage().getMaxEnergyStored()) {
            blockEntity.burnTime = 0;
            blockEntity.maxBurnTime = 0;
            return;
        }

        if(blockEntity.burnTime > 0) {
            blockEntity.setActive(true);
            blockEntity.burnTime--;
            int energyAdded = blockEntity.energyStorage.receiveEnergy(blockEntity.energyPerTick, false);
            if(energyAdded > 0)
                dirty = true;

        }
        if(blockEntity.burnTime <= 0) {
            ItemStack fuelStack = blockEntity.itemHandler.getStackInSlot(0);
            int burnTime = Utils.getBurnTime(fuelStack);

            if(burnTime > 0) {
                blockEntity.burnTime = burnTime;
                blockEntity.maxBurnTime = burnTime;

                fuelStack.shrink(1);
                dirty = true;
            } else  blockEntity.setActive(false);
        }
        if(dirty) {
            blockEntity.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }


        blockEntity.getDataAccess().set(0, blockEntity.energyStorage.getEnergyStored());
        blockEntity.getDataAccess().set(1, blockEntity.energyStorage.getMaxEnergyStored());
        blockEntity.getDataAccess().set(2, blockEntity.getProgressOfBurn());
    }

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energyStorage.getEnergyStored();
                case 1 -> energyStorage.getMaxEnergyStored();
                case 2 -> getProgressOfBurn();
                default -> -1;
            };
        }

        @Override
        public void set(int index, int value) {
            // Voliteľné – ak potrebuješ klient → server, inak ignoruj
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public int getProgressOfBurn() {
        return  (int) ((burnTime / (float) maxBurnTime) * 100);
    }

    public ContainerData getDataAccess() {
        return containerData;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.project_nuclear.coal_generator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new CoalGeneratorMenu(id, inv, this, this.dataAccess);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energyStorage.deserializeNBT(registries, tag.get("energyStorage"));
        Optional<CompoundTag> invTag = tag.getCompound("inventory");
        itemHandler.deserializeNBT(registries, invTag.orElse(null));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("energyStorage", energyStorage.serializeNBT(registries));
        tag.put("inventory", itemHandler.serializeNBT(registries));
    }

    public BurnableItemStackHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public void consumeProducedEnergy(int amount) {
        energyStorage.extractEnergy(amount, false);
    }

    public void setActive(boolean active) {
        if (this.active != active) {
            this.active = active;
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private static final ResourceLocation TEXTURE_FRONT_INACTIVE = ResourceLocation.fromNamespaceAndPath(Projectnuclear.MODID, "block/coal_generator");
    private static final ResourceLocation TEXTURE_FRONT_ACTIVE   = ResourceLocation.fromNamespaceAndPath(Projectnuclear.MODID, "block/coal_generator_active");
    private static final ResourceLocation TEXTURE_TOP            = ResourceLocation.fromNamespaceAndPath(Projectnuclear.MODID, "block/machine_casing");
    private static final ResourceLocation TEXTURE_MACHINE_BLOCK           = ResourceLocation.fromNamespaceAndPath(Projectnuclear.MODID, "block/machine_casing");

    @Override
    public ResourceLocation getMachineTexture() {
        return TEXTURE_MACHINE_BLOCK;
    }

    @Override
    public @Nullable MachineRenderer getSidedOverlay() {
        Direction front = this.getBlockState().getValue(CoalGenerator.FACING);
        return machineRenderer.addOverlayToSide(front, TEXTURE_FRONT_INACTIVE);
    }

    @Override
    public boolean isOverlayGlowing(Direction side) {
        // Only glow on front when active
        return side == this.getBlockState().getValue(CoalGenerator.FACING) && this.getBlockState().getValue(CoalGenerator.ACTIVE);
    }

    @Override
    public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.worldPosition);
        MenuProvider.super.writeClientSideData(menu, buffer);
    }

    @Override
    public int produceEnergy() {
        int voltage = getVoltage();
        int energyStored = energyStorage.getEnergyStored();
        return Math.min(voltage, energyStored);  // len vrát množstvo, neuberaj
    }

    @Override
    public int getVoltage() {
        return voltage;
    }

    @Override
    public EnergyNet getEnergyNet() {
        return net;
    }

    @Override
    public void setEnergyNet(EnergyNet net) {
        this.net = net;
    }

    @Override
    public void explode() {
        System.out.println("Exploding coal generator");
    }

    @Override
    public void onLoad() {
        super.onLoad();
        EnergyNetManager.register(this) ;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide()) {
            EnergyNetManager.enqueueUnregister(this);
        }
    }
}
