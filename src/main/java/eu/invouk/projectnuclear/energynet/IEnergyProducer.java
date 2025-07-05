package eu.invouk.projectnuclear.energynet;

import net.neoforged.neoforge.energy.EnergyStorage;

public interface IEnergyProducer extends IEnergyNode {
    int produceEnergy();
    EEnergyTier getEnergyTier();
    EnergyStorage getEnergyStorage();
    void consumeProducedEnergy(int amount);
}
