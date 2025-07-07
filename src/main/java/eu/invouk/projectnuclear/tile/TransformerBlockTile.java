package eu.invouk.projectnuclear.tile;

import com.mojang.datafixers.util.Pair;
import eu.invouk.projectnuclear.Projectnuclear;
import eu.invouk.projectnuclear.blocks.energy.BasicCable;
import eu.invouk.projectnuclear.blocks.energy.CoalGenerator;
import eu.invouk.projectnuclear.energynet.*;
import eu.invouk.projectnuclear.models.IOverlayRenderable;
import eu.invouk.projectnuclear.models.MachineRenderer;
import eu.invouk.projectnuclear.register.ModBlocksEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.energy.EnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class TransformerBlockTile extends BlockEntity implements IEnergyTransformer, IEnergyConsumer, IOverlayRenderable {

    private EnergyNet energyNet;
    private EEnergyTier inputTier = EEnergyTier.ULV;
    private final Queue<EnergyPacket> incomingPackets = new LinkedList<>();
    private int accumulatedEnergy = 0;
    private int accumulatedPacketCount = 0;

    private final ETransformerState transformerState = ETransformerState.STEP_UP;

    private final MachineRenderer machineRenderer;

    public TransformerBlockTile(BlockPos pos, BlockState state) {
        super(ModBlocksEntities.TRANSFORMER_BLOCK_TILE.get(), pos, state);

        machineRenderer = new MachineRenderer();
    }

    @Override
    public void receivePacket(EnergyPacket packet) {
        incomingPackets.add(packet);
    }

    public void tick() {
        if (level.isClientSide) return;
        if (energyNet == null) return;



/*
        if(!incomingPackets.isEmpty())
            System.out.println("[Transformer] Tick start, incomingPackets size: " + incomingPackets.size());
        if (energyNet == null) {
            System.out.println("[Transformer] energyNet is null!");
        } else {
            //System.out.println(worldPosition);
            BlockPos outputPos = worldPosition.relative(outputDir);
            List<Pair<IEnergyConsumer, Queue<BlockPos>>> consumersWithPaths = energyNet.findPathsToAllConsumers(outputPos);
            consumersWithPaths.removeIf(entry -> entry.getFirst() == this);
            if(!consumersWithPaths.isEmpty()) {
                System.out.println("[Transformer] Found consumers: " + consumersWithPaths.size() + ".");
                for (Pair<IEnergyConsumer, Queue<BlockPos>> consumersWithPath : consumersWithPaths) {
                    if(consumersWithPath.getFirst() instanceof BasicBatteryBufferTile bufferTile) {
                        System.out.println("Tile stored: " + bufferTile.getEnergyStorage().getEnergyStored());
                        System.out.println("Tile max stored: " + bufferTile.getEnergyStorage().getMaxEnergyStored());
                        System.out.println("prio: " + bufferTile.getPriority());
                        System.out.println("Alive: " + bufferTile.isAlive());
                    }
                }
            } /*else {
                for (IEnergyNode node : energyNet.getNodes()) {
                    System.out.println("Node: " + node);
                }
                System.out.println("Consumers = null");
            }*
        }*/


        while (!incomingPackets.isEmpty()) {
            EnergyPacket packet = incomingPackets.poll();


            if (transformerState == ETransformerState.STEP_UP) {
                int targetVoltage = getOutputTier().getMaxTransferPerTick();
                if (packet.voltage < targetVoltage) {
                    accumulatedEnergy += packet.energy;
                    accumulatedPacketCount++;

                    int accumulationRatio = (int) Math.ceil((double) targetVoltage / packet.voltage);

                    // vždy posielame na opačný smer od facing:
                    Direction inputFacing = getBlockState().getValue(BlockStateProperties.FACING);
                    //Direction outputDir = inputFacing.getOpposite();
                    BlockPos outputPos = worldPosition.relative(inputFacing);

                    if(level.getBlockEntity(outputPos) instanceof BasicCableTile basicCable) {
                        System.out.println("THIS IS CABLE TO OUTPUT FROM TRANSFORMATOR");
                    } else {
                            System.out.println(level.getBlockEntity(outputPos));
                    }

                    List<Pair<IEnergyConsumer, Queue<BlockPos>>> consumersWithPaths = energyNet.findPathsToAllConsumers(outputPos);
                    consumersWithPaths.removeIf(entry -> entry.getFirst() == this);

                    if (consumersWithPaths.isEmpty()) continue;

                    if (accumulatedPacketCount >= accumulationRatio) {
                        for (Pair<IEnergyConsumer, Queue<BlockPos>> entry : consumersWithPaths) {
                            if (entry.getSecond() == null || entry.getSecond().isEmpty()) continue;
                            Queue<BlockPos> fullPath = new LinkedList<>(entry.getSecond());

                            if (!fullPath.isEmpty()) {
                                BlockPos nextPos = fullPath.poll(); // vyber prvý cieľ v ceste

                                EnergyPacket newPacket = new EnergyPacket(
                                        accumulatedEnergy, // alebo packet.energy podľa kontextu
                                        targetVoltage,
                                        worldPosition,     // odkiaľ packet ide
                                        nextPos,           // kam ide ako prvý hop
                                        fullPath           // zvyšná cesta po prvom hope
                                );
                                energyNet.addPacket(newPacket);
                            }
                        }
                        accumulatedEnergy = 0;
                        accumulatedPacketCount = 0;
                    }
                }
            } else if (transformerState == ETransformerState.STEP_DOWN) {
                // vždy posielame na opačný smer od facing:
                Direction inputFacing = getBlockState().getValue(BlockStateProperties.FACING);
                Direction outputDir = inputFacing.getOpposite();
                BlockPos outputPos = worldPosition.relative(outputDir);
                List<Pair<IEnergyConsumer, Queue<BlockPos>>> consumersWithPaths = energyNet.findPathsToAllConsumers(outputPos);
                consumersWithPaths.removeIf(entry -> entry.getFirst() == this);

                int targetVoltage = getOutputTier().getMaxTransferPerTick();
                if (packet.voltage > targetVoltage) {
                    int totalEnergy = packet.energy;
                    int ratio = (int) Math.ceil((double) packet.voltage / targetVoltage);

                    for (int i = 0; i < ratio && totalEnergy > 0; i++) {
                        int energyToSend = Math.min(totalEnergy, targetVoltage);
                        totalEnergy -= energyToSend;

                        for (Pair<IEnergyConsumer, Queue<BlockPos>> entry : consumersWithPaths) {
                            if (entry.getSecond() == null || entry.getSecond().isEmpty()) continue;
                            Queue<BlockPos> fullPath = new LinkedList<>(entry.getSecond());

                            if (!fullPath.isEmpty()) {
                                BlockPos nextPos = fullPath.poll();
                                EnergyPacket newPacket = new EnergyPacket(
                                        energyToSend,
                                        targetVoltage,
                                        worldPosition,
                                        nextPos,
                                        new LinkedList<>(entry.getSecond())
                                );
                                energyNet.addPacket(newPacket);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putString("inputTier", inputTier.name());
        //tag.putString("outputTier", outputTier.name());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inputTier = EEnergyTier.valueOf(tag.getStringOr("inputTier", EEnergyTier.ULV.name()));
        //outputTier = EEnergyTier.valueOf(tag.getStringOr("outputTier", EEnergyTier.ULV.name()));
    }

    public EEnergyTier getInputTier() {
        return inputTier;
    }

    public void setInputTier(EEnergyTier inputTier) {
        this.inputTier = inputTier;
        setChanged();
    }

    public EEnergyTier getOutputTier() {
        return EEnergyTier.getTierUp(inputTier);
    }

    @Override
    public EnergyNet getEnergyNet() {
        return energyNet;
    }

    @Override
    public void setEnergyNet(EnergyNet net) {
        energyNet = net;
    }

    @Override
    public void explode() {
        System.out.println("Exploding Transformer");
    }

    @Override
    public BlockPos getPosition() {
        return getBlockPos();
    }

    private static final ResourceLocation TEXTURE_MACHINE_BLOCK           = ResourceLocation.fromNamespaceAndPath(Projectnuclear.MODID, "block/machine_casing");
    private static final ResourceLocation TEXTURE_OUTPUT_SIDE  = ResourceLocation.fromNamespaceAndPath(Projectnuclear.MODID, "block/battery_output_side");
    private static final ResourceLocation TEXTURE_INPUT_SIDE  = ResourceLocation.fromNamespaceAndPath(Projectnuclear.MODID, "block/transformer_input_side");

    @Override
    public @Nullable ResourceLocation getMachineTexture() {
        return TEXTURE_MACHINE_BLOCK;
    }

    @Override
    public @Nullable MachineRenderer getSidedOverlay() {
        Direction front = this.getBlockState().getValue(CoalGenerator.FACING);
        Direction opposite_front = this.getBlockState().getValue(CoalGenerator.FACING).getOpposite();
        return machineRenderer.addOverlayToSide(front, TEXTURE_OUTPUT_SIDE).addOverlayToSide(opposite_front, TEXTURE_INPUT_SIDE);
    }

    @Override
    public int consumeEnergy(int available, int voltage) {
        int accepted = Math.min(available, voltage);
        if (accepted > 0) {
            receivePacket(new EnergyPacket(accepted, voltage, worldPosition, worldPosition, new LinkedList<>()));
        }
        return accepted;
    }


    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public EEnergyTier getEnergyTier() {
        return inputTier;
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public EnergyStorage getEnergyStorage() {
        return null;
    }
}
