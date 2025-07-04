package eu.invouk.projectnuclear.energynet;

import net.neoforged.neoforge.energy.EnergyStorage;

public interface IEnergyConsumer extends IEnergyNode{
    int consumeEnergy(int available, int voltage);
    int getPriority();
    int getVoltage();
    boolean isAlive();
    EnergyStorage getEnergyStorage();
}
