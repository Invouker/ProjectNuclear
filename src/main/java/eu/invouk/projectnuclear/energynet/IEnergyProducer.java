package eu.invouk.projectnuclear.energynet;

import net.neoforged.neoforge.energy.EnergyStorage;

public interface IEnergyProducer extends IEnergyNode {
    int produceEnergy();
    int getVoltage();
    EnergyStorage getEnergyStorage();
    void consumeProducedEnergy(int amount);
}
