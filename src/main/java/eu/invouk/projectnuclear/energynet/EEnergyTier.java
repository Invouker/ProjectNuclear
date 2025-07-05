package eu.invouk.projectnuclear.energynet;

public enum EEnergyTier {
    ULV(24, "ULV"),
    LV(240, ""),
    MV(400, ""),
    HV(1000, ""),
    EV(3500, ""),
    IV(22000, "");

    private final int maxTransferPerTick;

    EEnergyTier(int maxTransferPerTick, String name) {
        this.maxTransferPerTick = maxTransferPerTick;
    }

    public int getMaxTransferPerTick() {
        return maxTransferPerTick;
    }

    @Override
    public String toString() {
        return "EEnergyTier{" +
                "maxTransferPerTick=" + maxTransferPerTick +
                '}';
    }
}