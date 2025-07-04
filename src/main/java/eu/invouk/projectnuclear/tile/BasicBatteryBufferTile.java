package eu.invouk.projectnuclear.tile;

import eu.invouk.projectnuclear.Projectnuclear;
import eu.invouk.projectnuclear.blocks.CoalGenerator;
import eu.invouk.projectnuclear.energynet.*;
import eu.invouk.projectnuclear.models.IOverlayRenderable;
import eu.invouk.projectnuclear.models.MachineRenderer;
import eu.invouk.projectnuclear.register.ModBlocksEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import org.jetbrains.annotations.Nullable;

public class BasicBatteryBufferTile extends BlockEntity implements IOverlayRenderable, IEnergyConsumer, IEnergyProducer {

    private EnergyNet net;
    private final int voltage = 32;
    private final MachineRenderer machineRenderer;


    private final EnergyStorage energyStorage = new EnergyStorage(100000, voltage) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (!simulate && received > 0) {
                // Zavoláme metódu na označenie zmeny a odoslanie paketu
                onEnergyChanged();
            }
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = super.extractEnergy(maxExtract, simulate);
            if (!simulate && extracted > 0) {
                // Zavoláme metódu na označenie zmeny a odoslanie paketu
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

    public BasicBatteryBufferTile( BlockPos pos, BlockState blockState) {
        super(ModBlocksEntities.BASIC_BATTERY_BUFFER.get(), pos, blockState);
        machineRenderer = new MachineRenderer();
    }

    @Override
    public BlockPos getPosition() {
        return this.worldPosition;
    }


    @Override
    public int consumeEnergy(int available, int voltage) {
        if (voltage > this.voltage) {
            explode();
            return 0;
        }

        // Neprijímaj energiu z output smeru
        if (net != null) {
            Direction outputDirection = this.getBlockState().getValue(CoalGenerator.FACING);
            BlockEntity from = level.getBlockEntity(worldPosition.relative(outputDirection));
            if (from instanceof IEnergyProducer) {
                return 0;
            }
        }

        int accept = Math.min(energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored(), available);
        energyStorage.receiveEnergy(accept, false);
        return accept;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean isAlive() {
        return !this.isRemoved();
    }

    @Override
    public int produceEnergy() {
        if (net == null) return 0;

        // Ak nemáme energiu, nemáme čo produkovať
        if (energyStorage.getEnergyStored() <= 0) return 0;

        // Skontroluj, či na outputDirection je niekto, kto vie prijímať energiu
        Direction outputDirection = this.getBlockState().getValue(CoalGenerator.FACING);
        BlockEntity be = level.getBlockEntity(worldPosition.relative(outputDirection));
        if (be == null) return 0;

        // Ak tam je IEnergyConsumer alebo IBufferEnergyConsumer alebo kábel
        if (!(be instanceof IEnergyConsumer) &&
                !(be instanceof IBufferEnergyConsumer) &&
                !(be instanceof IEnergyCable)) {
            return 0; // na výstupe nie je konzument ani kábel
        }

        // Vypočítaj koľko môže batéria maximálne poslať na tick
        return Math.min(energyStorage.getEnergyStored(), 50);
    }
    @Override
    public int getVoltage() {
        return voltage;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if(tag.contains("energyStorage"))
            energyStorage.deserializeNBT(registries, tag.get("energyStorage"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("energyStorage", energyStorage.serializeNBT(registries));
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

    @Override
    public EnergyStorage getEnergyStorage() {
        return energyStorage;
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
        System.out.println("Explode energy buffer");
    }

    @Override
    public void onLoad() {
        super.onLoad();
        EnergyNetManager.register(this); ;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide()) {
            EnergyNetManager.enqueueUnregister(this);
        }
    }

    @Override
    public void consumeProducedEnergy(int amount) {
        energyStorage.extractEnergy(amount, false);
    }

    @Override
    public boolean canAcceptEnergyFrom(Direction directionToConsumer) {
        Direction front = this.getBlockState().getValue(CoalGenerator.FACING);
        return front != directionToConsumer;
    }

    private static final ResourceLocation TEXTURE_OUTPUT_SIDE  = ResourceLocation.fromNamespaceAndPath(Projectnuclear.MODID, "block/battery_output_side");
    private static final ResourceLocation TEXTURE_MACHINE_BLOCK           = ResourceLocation.fromNamespaceAndPath(Projectnuclear.MODID, "block/machine_casing");


    @Override
    public @Nullable ResourceLocation getMachineTexture() {
        return TEXTURE_MACHINE_BLOCK;
    }

    @Override
    public @Nullable MachineRenderer getSidedOverlay() {
        Direction front = this.getBlockState().getValue(CoalGenerator.FACING);
        return machineRenderer.addOverlayToSide(front, TEXTURE_OUTPUT_SIDE);
    }
}
