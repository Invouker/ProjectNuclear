package eu.invouk.projectnuclear.energynet;

public interface IEnergyCable extends IEnergyNode {
    int getVoltageRating();
    int getCapacity();
}
