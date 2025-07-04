package eu.invouk.projectnuclear.energynet;

import eu.invouk.projectnuclear.tile.BasicCableTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class EnergyNetManager {

    //private static final Set<EnergyNet> allNets = Collections.synchronizedSet(new HashSet<>());
    private static final CopyOnWriteArrayList<EnergyNet> allNets = new CopyOnWriteArrayList<>();

    private static final Queue<IEnergyNode> rebuildQueue = new LinkedList<>();
    private static final Set<IEnergyNode> queuedNodes = new HashSet<>();
    private static final Queue<IEnergyNode> unregisterQueue = new LinkedList<>();

    public static void register(IEnergyNode node) {
        rebuildEnergyNetFrom(node);
    }

    public static void unregister(IEnergyNode node) {
        EnergyNet net = node.getEnergyNet();
        if (net != null) {
            net.removeNode(node);
            if (net.isEmpty()) {
                net.setValid(false);
                removeNet(net);
            }
        }
        node.setEnergyNet(null);

        BlockEntity nodeBe = (BlockEntity) node;
        Level level = nodeBe.getLevel();
        if (level == null || level.isClientSide()) return;

        BlockPos pos = nodeBe.getBlockPos();
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockEntity neighborBe = level.getBlockEntity(neighborPos);
            if (neighborBe instanceof IEnergyNode neighborNode) {
                EnergyNet neighborNet = neighborNode.getEnergyNet();
                if (neighborNet == null || !neighborNet.isValid()) {
                    // namiesto okamžitého rebuild voláme enqueue
                    enqueueRebuild(neighborNode);
                }
            }
        }
    }

    private static void enqueueRebuild(IEnergyNode node) {
        if (queuedNodes.add(node)) {  // pridáme iba ak ešte nie je v fronte
            rebuildQueue.add(node);
        }
    }

    public static void processRebuildQueue() {
        while (!rebuildQueue.isEmpty()) {
            IEnergyNode node = rebuildQueue.poll();
            queuedNodes.remove(node);
            rebuildEnergyNetFrom(node);
        }
    }

    public static void rebuildEnergyNetFrom(IEnergyNode start) {
        System.out.println("RebuildEnergyNet Start");
        BlockEntity startBe = (BlockEntity) start;
        Level level = startBe.getLevel();
        if (level == null || level.isClientSide()) return;
        if (!(level instanceof ServerLevel serverLevel)) throw new IllegalStateException("EnergyNet must be created on server!");

        Set<IEnergyNode> visited = new HashSet<>();
        Queue<IEnergyNode> queue = new LinkedList<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            IEnergyNode current = queue.poll();
            if (!visited.add(current)) continue;

            EnergyNet currentNet = current.getEnergyNet();
            if (currentNet != null) {
                currentNet.removeNode(current);
                if (currentNet.isEmpty()) {
                    currentNet.setValid(false);
                    removeNet(currentNet);
                }
            }

            for (Direction dir : Direction.values()) {
                BlockPos adjPos = ((BlockEntity) current).getBlockPos().relative(dir);
                BlockEntity adjBe = level.getBlockEntity(adjPos);
                if (adjBe instanceof IEnergyNode adjNode && !visited.contains(adjNode)) queue.add(adjNode);
            }
        }

        EnergyNet newNet = new EnergyNet(serverLevel);
        for (IEnergyNode node : visited) newNet.registerNode(node);
        newNet.setValid(true);
        addNet(newNet);
        System.out.println("RebuildEnergyNet Finish");
    }

    public static void addNet(EnergyNet net) {
        System.out.println("AddNet Start");
        allNets.add(net);
        System.out.println("AddNet Finish");
    }

    public static void removeNet(EnergyNet net) {
        System.out.println("RemoveNet Start");
        allNets.remove(net);
        System.out.println("RemoveNet Finish");
    }

    public synchronized static void tick(Level level) {
        if (level.isClientSide()) return;
        synchronized (allNets) {
            Iterator<EnergyNet> it = allNets.iterator();
            while (it.hasNext()) {
                EnergyNet net = it.next();
                if (!net.isValid()) it.remove();
                else net.tick();
            }
        }
        processRebuildQueue();
        processUnregisterQueue();
    }


    public static void enqueueUnregister(IEnergyNode node) {
        synchronized (unregisterQueue) {
            unregisterQueue.add(node);
        }
    }

    // Nová metóda na spracovanie odregistrovania
    private static void processUnregisterQueue() {
        synchronized (unregisterQueue) {
            while (!unregisterQueue.isEmpty()) {
                IEnergyNode node = unregisterQueue.poll();
                if (node != null) {
                    unregister(node);
                }
            }
        }
    }
}
