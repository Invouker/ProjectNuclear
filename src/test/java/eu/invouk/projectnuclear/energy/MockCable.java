package eu.invouk.projectnuclear.energy;

import eu.invouk.projectnuclear.energynet.EnergyNet;
import eu.invouk.projectnuclear.energynet.IEnergyCable;

public class MockCable implements IEnergyCable {
    private final int voltageRating;
    private final int capacity;
    private final String name;
    private EnergyNet net;

    public MockCable(String name, int voltageRating, int capacity) {
        this.name = name;
        this.voltageRating = voltageRating;
        this.capacity = capacity;
    }

    @Override
    public int getVoltageRating() {
        return voltageRating;
    }

    @Override
    public int getCapacity() {
        return capacity;
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
        System.out.println("[EXPLODE] Cable " + name);
    }

    @Override
    public String getDebugInfo() {
        return "Cable-" + name;
    }
}
