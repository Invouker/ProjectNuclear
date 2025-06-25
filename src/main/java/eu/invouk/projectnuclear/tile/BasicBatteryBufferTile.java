package eu.invouk.projectnuclear.tile;

import eu.invouk.projectnuclear.energynet.EnergyNet;
import eu.invouk.projectnuclear.energynet.IEnergyConsumer;
import eu.invouk.projectnuclear.energynet.IEnergyProducer;
import eu.invouk.projectnuclear.register.ModBlocksEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BasicBatteryBufferTile extends BlockEntity implements IEnergyProducer, IEnergyConsumer {

    private EnergyNet net;
    private final int capacity = 10000;
    private int stored;
    private final int voltage = 32;

    public BasicBatteryBufferTile( BlockPos pos, BlockState blockState) {
        super(ModBlocksEntities.BASIC_BATTERY_BUFFER.get(), pos, blockState);
    }

    @Override
    public int consumeEnergy(int available, int voltage) {
        if (voltage != this.voltage) return 0;
        int accept = Math.min(capacity - stored, available);
        stored += accept;
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
        int out = Math.min(50, stored);
        stored -= out;
        return out;
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
        System.out.println("Explode energy buffer");
    }



    @Override
    public String getDebugInfo() {
        return String.format(
                "%s [pos=%s, voltage=%dV, netValid=%s]",
                getClass().getSimpleName(),
                getBlockPos(),
                getVoltage(),
                getEnergyNet() != null && getEnergyNet().isValid()
        );
    }
}
