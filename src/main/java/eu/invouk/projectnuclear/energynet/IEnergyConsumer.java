package eu.invouk.projectnuclear.energynet;

public interface IEnergyConsumer extends IEnergyNode{
    int consumeEnergy(int available, int voltage);
    int getPriority();
    int getVoltage();
    boolean isAlive();
}
