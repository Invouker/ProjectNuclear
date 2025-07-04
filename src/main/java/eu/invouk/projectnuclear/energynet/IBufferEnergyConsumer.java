package eu.invouk.projectnuclear.energynet;

import net.minecraft.core.Direction;

public interface IBufferEnergyConsumer extends IEnergyConsumer {

    int storeEnergy(int excessWatts, int voltage);
    boolean canAcceptEnergyFrom(Direction directionToConsumer);
}
