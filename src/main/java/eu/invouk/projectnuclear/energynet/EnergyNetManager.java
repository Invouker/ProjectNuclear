package eu.invouk.projectnuclear.energynet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EnergyNetManager {

    private static final Set<EnergyNet> allNets = Collections.synchronizedSet(new HashSet<>());

    public static void register(IEnergyNode node) {
        // Hľadaj pripojené siete
        Set<EnergyNet> neighborNets = new HashSet<>();
        Level level = ((BlockEntity) node).getLevel();
        BlockPos pos = ((BlockEntity) node).getBlockPos();

        for (BlockPos offset : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            if (offset.equals(pos)) continue;

            BlockEntity neighbor = level.getBlockEntity(offset);
            if (neighbor instanceof IEnergyNode neighborNode) {
                EnergyNet net = neighborNode.getEnergyNet();
                if (net != null && net.isValid()) {
                    neighborNets.add(net);
                }
            }
        }

        EnergyNet use;
        if (neighborNets.isEmpty()) {
            use = new EnergyNet();
            allNets.add(use);
        } else {
            // Zober prvú sieť a prípadné ďalšie pripoj
            use = neighborNets.iterator().next();
            for (EnergyNet other : neighborNets) {
                if (other != use) {
                    use.merge(other);
                    allNets.remove(other);
                }
            }
        }

        if (node instanceof IEnergyProducer p) use.addProducer(p);
        if (node instanceof IEnergyConsumer c) use.addConsumer(c);
        if (node instanceof IEnergyCable cable) use.addCable(cable);

        node.setEnergyNet(use);
    }

    public static void unregister(IEnergyNode node) {
        EnergyNet net = node.getEnergyNet();
        if (net != null) {
            net.removeNode(node);
            if (net.getAllNodes().isEmpty()) {
                net.setValid(false);
                allNets.remove(net);
            }
        }
    }

    public static void tick(Level level) {
        // Zavolaj raz za tick — len na server strane
        if (level.isClientSide) return;

        for (EnergyNet net : allNets) {
            if (net.isValid()) {
                net.tick();
            }
        }
    }
}
