package eu.invouk.projectnuclear.energy;

import eu.invouk.projectnuclear.energynet.EnergyNet;
import eu.invouk.projectnuclear.energynet.IEnergyProducer;
import net.neoforged.neoforge.energy.EnergyStorage;

public class MockProducer implements IEnergyProducer {
    private final int voltage;
    private final EnergyStorage storage;
    private final String name;
    private EnergyNet net;

    public MockProducer(String name, int voltage, int bufferCapacity) {
        this.name = name;
        this.voltage = voltage;
        this.storage = new EnergyStorage(bufferCapacity, 128);
    }

    @Override
    public int produceEnergy() {
        // Simuluje generátor: vyrobí energiu iba ak je v bufferi miesto
        if (storage.getEnergyStored() >= storage.getMaxEnergyStored()) return 0;
        return voltage * 4; // napr. 4A packet
    }

    @Override
    public int getVoltage() {
        return voltage;
    }

    @Override
    public EnergyStorage getEnergyStorage() {
        return storage;
    }

    @Override
    public void consumeProducedEnergy(int amount) {
        storage.extractEnergy(amount, false);
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
        System.out.println("[EXPLODE] Producer " + name);
    }

    @Override
    public String getDebugInfo() {
        return "Producer-" + name;
    }
}
