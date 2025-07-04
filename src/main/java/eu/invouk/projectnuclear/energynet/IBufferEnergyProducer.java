package eu.invouk.projectnuclear.energynet;

public interface IBufferEnergyProducer extends IEnergyProducer {

    int storeEnergy(int excessWatts, int voltage);
}
