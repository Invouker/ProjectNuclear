package eu.invouk.projectnuclear.tile;

import eu.invouk.projectnuclear.energynet.EnergyNet;
import eu.invouk.projectnuclear.energynet.IEnergyCable;
import eu.invouk.projectnuclear.energynet.IEnergyNode;
import eu.invouk.projectnuclear.register.ModBlocksEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BasicCableTile extends BlockEntity implements IEnergyCable {

    private EnergyNet energyNet;
    private final int capacity;
    private final int voltageRating;

    public BasicCableTile(BlockPos blockPos, BlockState blockState) {
        super(ModBlocksEntities.BASIC_CABLE_TILE.get(), blockPos, blockState);
        this.capacity = 32;
        this.voltageRating = 32;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public int getVoltageRating() {
        return voltageRating;
    }

    @Override
    public EnergyNet getEnergyNet() {
        return energyNet;
    }

    @Override
    public void setEnergyNet(EnergyNet energyNet) {
        this.energyNet = energyNet;
    }

    @Override
    public void explode() {
        System.out.println("Explode it!");
        level.destroyBlock(getBlockPos(), false);
    }

    @Override
    public String getDebugInfo() {
        return String.format(
                "%s [pos=%s, capacity=%dV,, voltageRating=%dV, netValid=%s]",
                getClass().getSimpleName(),
                getBlockPos(),
                getCapacity(),
                getVoltageRating(),
                getEnergyNet() != null && getEnergyNet().isValid()
        );
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level.isClientSide) return;

        Set<EnergyNet> nets = new HashSet<>();
        for (Direction dir : Direction.values()) {
            BlockEntity be = level.getBlockEntity(worldPosition.relative(dir));
            if (be instanceof IEnergyNode node && node.getEnergyNet() != null) {
                nets.add(node.getEnergyNet());
            }
        }

        if (nets.isEmpty()) {
            EnergyNet net = new EnergyNet();
            this.setEnergyNet(net);
            net.addCable(this);
        } else {
            Iterator<EnergyNet> it = nets.iterator();
            EnergyNet primary = it.next();
            this.setEnergyNet(primary);
            primary.addCable(this);
            while (it.hasNext()) {
                primary.merge(it.next());
            }
        }
    }
}
