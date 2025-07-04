package eu.invouk.projectnuclear.energynet;


import net.minecraft.core.BlockPos;

public interface IEnergyNode {
    EnergyNet getEnergyNet();
    void setEnergyNet(EnergyNet net);
    void explode();

    BlockPos getPosition();

    default String getDebugInfo() {
        return this.getClass().getSimpleName();
    }
}
