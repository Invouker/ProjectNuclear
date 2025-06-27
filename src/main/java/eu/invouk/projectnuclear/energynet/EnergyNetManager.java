package eu.invouk.projectnuclear.energynet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

public class EnergyNetManager {

    private static final Set<EnergyNet> allNets = Collections.synchronizedSet(new HashSet<>());

    public static void register(IEnergyNode node) {
        rebuildEnergyNetFrom(node);
    }

    public static void unregister(IEnergyNode node) {
        EnergyNet net = node.getEnergyNet();
        if (net != null) {
            net.removeNode(node);
            net.setValid(false);
            allNets.remove(net);
        }
        node.setEnergyNet(null);

        // Rekonštrukcia sietí okolo zničeného alebo odstráneného node
        Level level = ((BlockEntity) node).getLevel();
        BlockPos pos = ((BlockEntity) node).getBlockPos();

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockEntity neighbor = level.getBlockEntity(neighborPos);

            if (neighbor instanceof IEnergyNode energyNode) {
                if (energyNode.getEnergyNet() == null || !energyNode.getEnergyNet().isValid()) {
                    rebuildEnergyNetFrom(energyNode);
                }
            }
        }
    }

    public static void rebuildEnergyNetFrom(IEnergyNode start) {
        BlockEntity startBe = (BlockEntity) start;
        Level level = startBe.getLevel();
        if (level == null || level.isClientSide()) return;

        // BFS na nájdenie všetkých susediacich node-ov z aktuálnej pozície
        Set<IEnergyNode> visited = new HashSet<>();
        Queue<IEnergyNode> queue = new LinkedList<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            IEnergyNode current = queue.poll();
            if (!visited.add(current)) continue;

            BlockEntity currentBe = (BlockEntity) current;
            BlockPos currentPos = currentBe.getBlockPos();

            for (Direction dir : Direction.values()) {
                BlockEntity neighbor = level.getBlockEntity(currentPos.relative(dir));
                if (neighbor instanceof IEnergyNode neighborNode) {
                    if (!visited.contains(neighborNode)) {
                        queue.add(neighborNode);
                    }
                }
            }
        }

        if (visited.isEmpty()) return;

        // Pred vytvorením novej siete odpoji všetky staré siete týchto node-ov
        for (IEnergyNode node : visited) {
            EnergyNet oldNet = node.getEnergyNet();
            if (oldNet != null) {
                oldNet.removeNode(node);
                if (oldNet.getAllNodes().isEmpty()) {
                    oldNet.setValid(false);
                    allNets.remove(oldNet);
                }
            }
            node.setEnergyNet(null);
        }

        // Vytvor novú sieť a pridaj všetky node-y
        EnergyNet newNet = new EnergyNet();
        for (IEnergyNode node : visited) {
            node.setEnergyNet(newNet);

            if (node instanceof IEnergyProducer p) newNet.addProducer(p);
            if (node instanceof IEnergyConsumer c) newNet.addConsumer(c);
            if (node instanceof IEnergyCable cable) newNet.addCable(cable);
        }
        newNet.setValid(true);
        allNets.add(newNet);
    }

    public static void tick(Level level) {
        if (level.isClientSide) return;

        synchronized (allNets) {
            Iterator<EnergyNet> it = allNets.iterator();
            while (it.hasNext()) {
                EnergyNet net = it.next();
                if (!net.isValid()) {
                    it.remove();
                    continue;
                }
                net.tick();
            }
        }
    }
}
