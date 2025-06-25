package eu.invouk.projectnuclear.energynet;

public interface IEnergyCable extends IEnergyNode {
    int getCapacity();
    int getVoltageRating();
}
