package eu.invouk.projectnuclear.energynet;

public interface IEnergyProducer extends IEnergyNode {
    int produceEnergy();
    int getVoltage();
}
