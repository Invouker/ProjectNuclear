package eu.invouk.projectnuclear.energynet;

public enum EEnergyTier {
    ULV(24, "ULV"),
    LV(240, "LV"),
    MV(400, "MV"),
    HV(1000, "HV"),
    EV(3500, "EV"),
    IV(22000, "IV");

    private final int maxTransferPerTick;
    private final String name;

    EEnergyTier(int maxTransferPerTick, String name) {
        this.maxTransferPerTick = maxTransferPerTick;
        this.name = name;
    }

    public int getMaxTransferPerTick() {
        return maxTransferPerTick;
    }

    public static EEnergyTier getTierUp(EEnergyTier eEnergyTier) {
        for (int i = 0; i < values().length; i++) {
            if(values()[i] == eEnergyTier)
                return values()[i+1];
        }
        return eEnergyTier;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "EEnergyTier{" +
                "maxTransferPerTick=" + maxTransferPerTick +
                ", name='" + name + '\'' +
                '}';
    }
}