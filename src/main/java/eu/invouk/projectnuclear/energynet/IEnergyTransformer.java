package eu.invouk.projectnuclear.energynet;

public interface IEnergyTransformer extends IEnergyNode {

    void receivePacket(EnergyPacket packet);
    EEnergyTier getInputTier();
}
