package eu.invouk.projectnuclear.energynet;

public interface IEnergyCable extends IEnergyNode {
    EEnergyTier getTier();

    /** For debugging purpose **/
    void addEnergyTransferredThisTick(int amount);
    void resetEnergyTransferredThisTick();
    int getEnergyTransferredThisTick();
    /** -- **/
}
