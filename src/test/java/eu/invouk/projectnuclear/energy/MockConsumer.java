package eu.invouk.projectnuclear.energy;

import eu.invouk.projectnuclear.energynet.EnergyNet;
import eu.invouk.projectnuclear.energynet.IEnergyConsumer;
import net.neoforged.neoforge.energy.EnergyStorage;

public class MockConsumer implements IEnergyConsumer {
    private final int priority;
    private final int voltage;
    private final EnergyStorage storage;
    private final String name;
    private EnergyNet net;

    public MockConsumer(String name, int voltage, int bufferCapacity, int priority) {
        this.name = name;
        this.voltage = voltage;
        this.priority = priority;
        this.storage = new EnergyStorage(bufferCapacity, 128);
    }

    @Override
    public int consumeEnergy(int available, int voltage) {
        return storage.receiveEnergy(available, false);
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public int getVoltage() {
        return voltage;
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public EnergyStorage getEnergyStorage() {
        return storage;
    }

    @Override
    public EnergyNet getEnergyNet() {
        return net;
    }

    @Override
    public void setEnergyNet(EnergyNet net) {
        this.net = net;
    }

    @Override
    public void explode() {
        System.out.println("[EXPLODE] Consumer " + name);
    }

    @Override
    public String getDebugInfo() {
        return "Consumer-" + name;
    }
}
