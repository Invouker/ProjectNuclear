package eu.invouk.projectnuclear.energynet;

import java.util.*;

public class EnergyNet {

    private static final boolean DEBUG = false;

    private final Set<IEnergyProducer> producers = new HashSet<>();
    private final Set<IEnergyConsumer> consumers = new HashSet<>();
    private final Set<IEnergyCable> cables = new HashSet<>();

    private boolean valid = true;

    public void addProducer(IEnergyProducer p) {
        producers.add(p);
    }

    public void addConsumer(IEnergyConsumer c) {
        consumers.add(c);
    }

    public void addCable(IEnergyCable c) {
        cables.add(c);
    }

    public void removeNode(IEnergyNode node) {
        producers.remove(node);
        consumers.remove(node);
        cables.remove(node);
    }

    public void tick() {
        if (!valid) return;

        if (DEBUG) {
            System.out.println("===== EnergyNet TICK =====");
            System.out.println("Producers: " + producers.size());
            System.out.println("Consumers: " + consumers.size());
            System.out.println("Cables: " + cables.size());
        }

        int totalFlow = 0;
        Map<Integer, Integer> energyByVoltage = new HashMap<>();

        for (IEnergyProducer producer : producers) {
            int voltage = producer.getVoltage();
            int produced = producer.produceEnergy();
            totalFlow += produced;
            energyByVoltage.put(voltage, energyByVoltage.getOrDefault(voltage, 0) + produced);
        }

        int maxFlow = cables.stream().mapToInt(IEnergyCable::getCapacity).sum();
        int maxVoltage = cables.stream().mapToInt(IEnergyCable::getVoltageRating).min().orElse(Integer.MAX_VALUE);


        if (totalFlow > maxFlow || energyByVoltage.keySet().stream().anyMatch(v -> v > maxVoltage)) {
            // Exploduj všetky
            for (IEnergyCable c : cables) c.explode();
            for (IEnergyConsumer c : consumers) c.explode();
            valid = false;
            return;
        }

        List<IEnergyConsumer> sortedConsumers = new ArrayList<>(consumers);
        sortedConsumers.removeIf(c -> !c.isAlive());
        sortedConsumers.sort(Comparator.comparingInt(IEnergyConsumer::getPriority).reversed());

        for (IEnergyConsumer c : sortedConsumers) {
            int voltage = c.getVoltage();
            int available = energyByVoltage.getOrDefault(voltage, 0);
            if (available <= 0) continue;

            int taken = c.consumeEnergy(available, voltage);
            energyByVoltage.put(voltage, Math.max(0, available - taken));
        }
    }

    public void merge(EnergyNet other) {
        if (other == this) return;

        this.producers.addAll(other.producers);
        this.consumers.addAll(other.consumers);
        this.cables.addAll(other.cables);

        for (IEnergyNode node : other.getAllNodes()) {
            node.setEnergyNet(this);
        }
        other.valid = false;
    }

    public Set<IEnergyNode> getAllNodes() {
        Set<IEnergyNode> all = new HashSet<>();
        all.addAll(producers);
        all.addAll(consumers);
        all.addAll(cables);
        return all;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
