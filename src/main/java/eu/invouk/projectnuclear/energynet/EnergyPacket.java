package eu.invouk.projectnuclear.energynet;

import net.minecraft.core.BlockPos;

import java.util.Queue;

public class EnergyPacket {
    public int energy;
    public final int voltage;
    public final BlockPos startPos;
    public BlockPos currentPos;
    public final Queue<BlockPos> path;

    public EnergyPacket(int energy, int voltage, BlockPos startPos, BlockPos currentPos, Queue<BlockPos> path) {
        this.energy = energy;
        this.voltage = voltage;
        this.startPos = startPos;
        this.currentPos = currentPos;
        this.path = path;
    }
}
