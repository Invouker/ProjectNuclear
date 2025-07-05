package eu.invouk.projectnuclear.energynet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class EnergyNet {

    private final ServerLevel level;
    private final Set<IEnergyNode> nodes = new HashSet<>();
    private final List<IEnergyProducer> producers = new ArrayList<>();
    private final List<IEnergyConsumer> consumers = new ArrayList<>();
    private final List<IEnergyCable> cables = new ArrayList<>();
    private boolean valid = true;
    private boolean isDirty = false;
    private boolean isHighlighted = false;

    public EnergyNet(ServerLevel level) {
        this.level = level;
        System.out.println("[EnergyNet] Created new EnergyNet on level " + level);
    }

    public synchronized void registerNode(IEnergyNode node) {
        System.out.println("RegisterNode Start");
        if (nodes.add(node)) {
            node.setEnergyNet(this);
            if (node instanceof IEnergyProducer p) producers.add(p);
            if (node instanceof IEnergyConsumer c) consumers.add(c);
            if (node instanceof IEnergyCable c) cables.add(c);
            isDirty = true;
            System.out.println("[EnergyNet] Registered node: " + node.getPosition() + ", total nodes: " + nodes.size());
        }
        System.out.println("RegisterNode Finish");
    }

    public synchronized void removeNode(IEnergyNode node) {

        System.out.println("RemovedNode Start");
        System.out.println("[EnergyNet] removeNode called during save? " + Thread.currentThread().getName());
        if (nodes.remove(node)) {
            node.setEnergyNet(null);
            producers.remove(node);
            consumers.remove(node);
            cables.remove(node);
            isDirty = true;
            System.out.println("[EnergyNet] Removed node: " + node.getPosition() + ", remaining nodes: " + nodes.size());
        }

        System.out.println("RemovedNode Finish");
    }

    public synchronized void invalidate() {
        System.out.println("[EnergyNet] Invalidating net with " + nodes.size() + " nodes");
        valid = false;
        isDirty = false;
        for (IEnergyNode node : new HashSet<>(nodes)) removeNode(node);
    }

    public synchronized void tick() {
        if (!valid) return;
        if (isDirty) {
            recalculateNetwork();
            isDirty = false;
        }

        List<IEnergyProducer> producersSnapshot = new ArrayList<>(producers);
        for (IEnergyProducer producer : producersSnapshot) {
            int producedEnergy = producer.produceEnergy();
            if (producedEnergy <= 0) continue;

            int voltage = producer.getEnergyTier().getMaxTransferPerTick();

            Map<IEnergyNode, Integer> visited = new HashMap<>();
            Queue<IEnergyNode> queue = new LinkedList<>();
            queue.add(producer);
            visited.put(producer, 0);

            boolean overvoltageDetected = false;
            AtomicInteger remainingEnergy = new AtomicInteger(producedEnergy);

            while (!queue.isEmpty()) {
                IEnergyNode node = queue.poll();
                int distance = visited.get(node);

                if (node != producer) {
                    if (node instanceof IEnergyCable cable) {
                        int cableLimit = cable.getTier().getMaxTransferPerTick();

                        // Overvoltage kontrola:
                        if (voltage > cableLimit) {
                            System.out.println("[EnergyNet] Overvoltage! Exploding cable at " + ((BlockEntity) cable).getBlockPos());
                            cable.explode();
                            overvoltageDetected = true;
                            break;
                        }

                        // Limituj prenos podľa kabeláže:
                        int transferable = Math.min(remainingEnergy.get(), cableLimit);
                        remainingEnergy.set(transferable);

                    } else if (node instanceof IEnergyConsumer consumer) {
                        if (voltage > consumer.getEnergyTier().getMaxTransferPerTick()) {
                            System.out.println("[EnergyNet] Overvoltage! Exploding consumer at " + ((BlockEntity) consumer).getBlockPos());
                            consumer.explode();
                            overvoltageDetected = true;
                            break;
                        }

                        if (consumer.isAlive() && consumer.getEnergyTier().getMaxTransferPerTick() == voltage) {
                            int consumed = consumer.consumeEnergy(remainingEnergy.get(), voltage);
                            remainingEnergy.addAndGet(-consumed);
                            producer.consumeProducedEnergy(consumed);
                        }
                    }
                }

                for (IEnergyNode neighbor : getAdjacentNodes(node)) {
                    if (!visited.containsKey(neighbor)) {
                        BlockPos currentPos = ((BlockEntity) node).getBlockPos();
                        BlockPos neighborPos = ((BlockEntity) neighbor).getBlockPos();

                        Direction directionToNeighbor = Direction.getNearest(
                                currentPos.getX() - neighborPos.getX(),
                                currentPos.getY() - neighborPos.getY(),
                                currentPos.getZ() - neighborPos.getZ(),
                                Direction.UP);

                        if (neighbor instanceof IEnergyConsumer consumer) {
                            if (!consumer.canAcceptEnergyFrom(directionToNeighbor)) continue;
                        }

                        visited.put(neighbor, distance + 1);
                        queue.add(neighbor);
                    }
                }
            }

            if (remainingEnergy.get() <= 0) {
                System.out.println("[EnergyNet] Energy fully distributed for producer at " + ((BlockEntity) producer).getBlockPos());
            }

            if (overvoltageDetected) {
                System.out.println("[EnergyNet] Overvoltage detected, producer at " + ((BlockEntity) producer).getBlockPos() + " exploded.");
                producer.explode();
            }
        }
    }


    private synchronized void recalculateNetwork() {
        System.out.println("[EnergyNet] Recalculating network connectivity...");
        Set<IEnergyNode> unvisited = new HashSet<>(nodes);
        List<Set<IEnergyNode>> components = new ArrayList<>();

        while (!unvisited.isEmpty()) {
            IEnergyNode startNode = unvisited.iterator().next();
            Set<IEnergyNode> component = findReachableNodes(startNode);
            components.add(component);
            unvisited.removeAll(component);
        }

        if (components.size() <= 1) {
            System.out.println("[EnergyNet] Network is fully connected (" + nodes.size() + " nodes)");
            return; // všetko v poriadku, nič nedeliť
        }

        System.out.println("[EnergyNet] Network split detected: " + components.size() + " components");

        // prvú komponentu nechaj v tejto sieti
        Set<IEnergyNode> firstComponent = components.remove(0);
        this.nodes.clear();
        this.producers.clear();
        this.consumers.clear();
        this.cables.clear();
        for (IEnergyNode node : firstComponent) registerNode(node);

        // ostatné komponenty vytvoria nové siete
        for (Set<IEnergyNode> component : components) {
            EnergyNet newNet = new EnergyNet(level);
            for (IEnergyNode node : component) newNet.registerNode(node);
            newNet.setValid(true);
            EnergyNetManager.addNet(newNet);
        }
    }

    private synchronized Set<IEnergyNode> findReachableNodes(IEnergyNode start) {
        Set<IEnergyNode> visited = new HashSet<>();
        Queue<IEnergyNode> queue = new LinkedList<>();
        visited.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            IEnergyNode current = queue.poll();
            for (IEnergyNode neighbor : getAdjacentNodes(current)) {
                if (visited.add(neighbor)) queue.add(neighbor);
            }
        }
        return visited;
    }

    private synchronized List<IEnergyNode> getAdjacentNodes(IEnergyNode node) {
        List<IEnergyNode> result = new ArrayList<>();
        BlockPos pos = node.getPosition();
        for (var dir : net.minecraft.core.Direction.values()) {
            BlockPos adjPos = pos.relative(dir);
            BlockEntity be = level.getBlockEntity(adjPos);
            if (be instanceof IEnergyNode neighbor) result.add(neighbor);
        }
        return result;
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public boolean isEmpty() { return nodes.isEmpty(); }
    public Set<IEnergyNode> getAllNodes() { return new HashSet<>(nodes); }

}
