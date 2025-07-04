package eu.invouk.projectnuclear.tile;

import eu.invouk.projectnuclear.energynet.EnergyNet;
import eu.invouk.projectnuclear.energynet.EnergyNetManager;
import eu.invouk.projectnuclear.energynet.IEnergyCable;
import eu.invouk.projectnuclear.register.ModBlocksEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BasicCableTile extends BlockEntity implements IEnergyCable {

    private EnergyNet energyNet;
    private static final int VOLTAGE_RATING = 32;
    private static final int CAPACITY = 1;

    public BasicCableTile(BlockPos blockPos, BlockState blockState) {
        super(ModBlocksEntities.BASIC_CABLE_TILE.get(), blockPos, blockState);
    }

    @Override
    public int getVoltageRating() {
        return VOLTAGE_RATING;
    }

    @Override
    public int getCapacity() {
        return CAPACITY;
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
    }

    @Override
    public BlockPos getPosition() {
        return this.worldPosition;
    }

    @Override
    public String getDebugInfo() {
        return String.format(
                "%s [pos=%s, voltageRating=%dV, netValid=%s]",
                getClass().getSimpleName(),
                getBlockPos(),
                getVoltageRating()
        );
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
}
