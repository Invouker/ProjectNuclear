package eu.invouk.projectnuclear.energynet;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

public class EnergyNet {

    private final ServerLevel level;
    private final Set<IEnergyNode> nodes = new HashSet<>();
    private final List<IEnergyProducer> producers = new ArrayList<>();
    //private final List<IEnergyConsumer> consumers = new ArrayList<>();
    //private final List<IEnergyCable> cables = new ArrayList<>();
    private boolean valid = true;
    private boolean isHighlighted = false;

    // 🟢 Toto je fronta aktívnych packetov v tejto sieti
    private final Queue<EnergyPacket> packetsInTransit = new LinkedList<>();

    public EnergyNet(ServerLevel level) {
        this.level = level;
    }

    public boolean isHighlighted() {
        return !isHighlighted;
    }

    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
    }

    public synchronized void registerNode(IEnergyNode node) {
        if (nodes.add(node)) {
            node.setEnergyNet(this);
            if (node instanceof IEnergyProducer producer) producers.add(producer);
            //if (node instanceof IEnergyConsumer consumer) consumers.add(consumer);
            //if (node instanceof IEnergyCable cable) cables.add(cable);
        }
    }

    public synchronized void removeNode(IEnergyNode node) {
        if (nodes.remove(node)) {
            node.setEnergyNet(null);
            producers.removeIf(iEnergyProducer -> iEnergyProducer instanceof IEnergyProducer);
            //consumers.removeIf(iEnergyProducer -> iEnergyProducer instanceof IEnergyConsumer);
            //cables.removeIf(iEnergyProducer -> iEnergyProducer instanceof IEnergyConsumer);
        }
    }

    public synchronized void tick() {
        if (!valid) return;

        // Reset energy transferred in cables
        for (IEnergyNode node : nodes) {
            if (node instanceof IEnergyCable cable) {
                cable.resetEnergyTransferredThisTick();
            }
        }

        // Process producers
        for (IEnergyProducer producer : producers) {
            int producedEnergy = producer.produceEnergy();
            if (producedEnergy > 0) {
                BlockPos start = ((BlockEntity) producer).getBlockPos();
                List<Pair<IEnergyConsumer, Queue<BlockPos>>> consumersWithPaths = findPathsToAllConsumers(start);
                if (!consumersWithPaths.isEmpty()) {
                    int energyPerConsumer = producedEnergy / consumersWithPaths.size();
                    int remainder = producedEnergy % consumersWithPaths.size();

                    for (Pair<IEnergyConsumer, Queue<BlockPos>> entry : consumersWithPaths) {
                        int energyForThis = energyPerConsumer;
                        if (remainder > 0) {
                            energyForThis++;
                            remainder--;
                        }
                        if (energyForThis > 0) {
                            EnergyPacket packet = new EnergyPacket(
                                    energyForThis,
                                    producer.getEnergyTier().getMaxTransferPerTick(),
                                    start,
                                    start,
                                    entry.getSecond()
                            );
                            packetsInTransit.add(packet);
                        }
                    }
                    producer.consumeProducedEnergy(producedEnergy);
                }
            }
        }

        // Process packets in transit
        Iterator<EnergyPacket> iterator = packetsInTransit.iterator();
        while (iterator.hasNext()) {
            EnergyPacket packet = iterator.next();

            if (packet.path.isEmpty()) {
                iterator.remove();
                continue;
            }

            BlockPos nextPos = packet.path.poll();
            packet.currentPos = nextPos;

            BlockEntity be = level.getBlockEntity(nextPos);

            if (be instanceof IEnergyCable cable) {
                int cableLimit = cable.getTier().getMaxTransferPerTick();
                if (packet.voltage > cableLimit) {
                    cable.explode();
                    iterator.remove();
                    continue;
                }
                cable.addEnergyTransferredThisTick(Math.min(packet.energy, packet.voltage));
            } else if (be instanceof IEnergyConsumer consumer) {
                int consumerLimit = consumer.getEnergyTier().getMaxTransferPerTick();
                if (packet.voltage > consumerLimit) {
                    consumer.explode();
                    iterator.remove();
                    continue;
                }
                int accepted = consumer.consumeEnergy(packet.energy, packet.voltage);
                packet.energy -= accepted;
                if (packet.energy <= 0) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Find paths to all consumers using BFS.
     */
    public List<Pair<IEnergyConsumer, Queue<BlockPos>>> findPathsToAllConsumers(BlockPos startPos) {
        List<Pair<IEnergyConsumer, Queue<BlockPos>>> result = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<Pair<BlockPos, Queue<BlockPos>>> queue = new LinkedList<>();
        queue.add(Pair.of(startPos, new LinkedList<>()));

        //System.out.println("[EnergyNet] Starting BFS from: " + startPos);

        while (!queue.isEmpty()) {
            Pair<BlockPos, Queue<BlockPos>> current = queue.poll();
            BlockPos currentPos = current.getFirst();
            Queue<BlockPos> pathSoFar = current.getSecond();

            if (!visited.add(currentPos)) {
                System.out.println("[EnergyNet] Already visited: " + currentPos);
                continue;
            }

            BlockEntity be = level.getBlockEntity(currentPos);
            //System.out.println("[EnergyNet] Visiting: " + currentPos + " BlockEntity: " + (be != null ? be.getClass().getSimpleName() : "null"));

            if (be instanceof IEnergyConsumer || be instanceof IEnergyTransformer ) {
                IEnergyConsumer consumer = (IEnergyConsumer) be;
                Queue<BlockPos> path = new LinkedList<>(pathSoFar);
                path.add(currentPos);
                System.out.println("[EnergyNet] Found consumer/transformer at: " + currentPos + " Path length: " + path.size());
                result.add(Pair.of(consumer, path));
                // continue searching for other consumers
                continue;
            }

            if (be instanceof IEnergyCable || be instanceof IEnergyProducer) {
                for (Direction dir : Direction.values()) {
                    BlockPos adj = currentPos.relative(dir);
                    if (visited.contains(adj)) {
                        System.out.println("[EnergyNet] Adjacent already visited: " + adj);
                        continue;
                    }
                    //System.out.println("[EnergyNet] Adding adjacent: " + adj + " to queue");
                    Queue<BlockPos> newPath = new LinkedList<>(pathSoFar);
                    newPath.add(currentPos);
                    queue.add(Pair.of(adj, newPath));
                }
            } else {
                //System.out.println("[EnergyNet] BlockEntity at " + currentPos + " is not cable/transformer/producer, skipping neighbors.");
            }
        }

        System.out.println("[EnergyNet] BFS complete. Found " + result.size() + " consumers.");
        return result;
    }


    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public boolean isEmpty() { return nodes.isEmpty(); }

    public Set<IEnergyNode> getNodes() {
        return nodes;
    }

    public synchronized void addPacket(EnergyPacket packet) {
        packetsInTransit.add(packet);
    }

    public Queue<EnergyPacket> getPacketsInTransit() {
        return packetsInTransit;
    }

}
