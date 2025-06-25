package eu.invouk.projectnuclear.energynet;


public interface IEnergyNode {
    EnergyNet getEnergyNet();
    void setEnergyNet(EnergyNet net);
    void explode();

    default String getDebugInfo() {
        return this.getClass().getSimpleName();
    }
}
