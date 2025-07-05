package eu.invouk.projectnuclear.energynet;

import net.neoforged.neoforge.energy.EnergyStorage;

public interface IEnergyProducer extends IEnergyNode {
    int produceEnergy();
    void consumeProducedEnergy(int amount);
    EEnergyTier getEnergyTier();
    EnergyStorage getEnergyStorage();
}
