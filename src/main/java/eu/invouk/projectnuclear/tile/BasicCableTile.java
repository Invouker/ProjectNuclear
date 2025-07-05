package eu.invouk.projectnuclear.tile;

import eu.invouk.projectnuclear.energynet.EEnergyTier;
import eu.invouk.projectnuclear.energynet.EnergyNet;
import eu.invouk.projectnuclear.energynet.EnergyNetManager;
import eu.invouk.projectnuclear.energynet.IEnergyCable;
import eu.invouk.projectnuclear.register.ModBlocksEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BasicCableTile extends BlockEntity implements IEnergyCable {

    private EnergyNet energyNet;

    public BasicCableTile(BlockPos blockPos, BlockState blockState) {
        super(ModBlocksEntities.BASIC_CABLE_TILE.get(), blockPos, blockState);
    }

    @Override
    public EnergyNet getEnergyNet() {
        return energyNet;
    }

    @Override
    public void setEnergyNet(EnergyNet energyNet) {
        this.energyNet = energyNet;
    }

    @Override
    public void explode() {
        System.out.println("Explode cable! due to overvoltage.");
        //level.destroyBlock(getBlockPos(), false);
        var pos = this.getBlockPos();
        level.explode(null, pos.getX(), pos.getY(), pos.getZ(), 0.1f, Level.ExplosionInteraction.BLOCK);
        //level.playLocalSound(this.getBlockPos(), , SoundSource.BLOCKS, 1f, 1f, true);
    }

    @Override
    public BlockPos getPosition() {
        return this.worldPosition;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level == null || level.isClientSide) return;

        EnergyNetManager.register(this);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide()) {
            EnergyNetManager.enqueueUnregister(this);
        }
    }

    @Override
    public EEnergyTier getTier() {
        return EEnergyTier.ULV;
    }


    /* For debugging purpose */
    private int energyTransferredThisTick = 0;

    @Override
    public void addEnergyTransferredThisTick(int amount) {
        energyTransferredThisTick += amount;
        System.out.println("Cable at " + getPosition() + " transferred += " + amount);
    }

    @Override
    public void resetEnergyTransferredThisTick() {
        energyTransferredThisTick = 0;
    }

    @Override
    public int getEnergyTransferredThisTick() {
        return energyTransferredThisTick;
    }


}
