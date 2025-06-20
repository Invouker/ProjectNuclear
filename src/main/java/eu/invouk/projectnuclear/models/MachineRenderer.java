package eu.invouk.projectnuclear.models;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MachineRenderer {

    private Map<Direction, ResourceLocation> overlayMap;

    public MachineRenderer() {
        overlayMap = new HashMap<>();
    }

    public MachineRenderer(Map<Direction, ResourceLocation> overlayMap) {
        this.overlayMap = overlayMap;
    }

    public MachineRenderer addOverlayToSide(Direction side, ResourceLocation resourceLocation) {
        overlayMap.put(side, resourceLocation);
        return this;
    }

    public @NotNull Map<Direction, ResourceLocation> build() {
        if(overlayMap == null)
            overlayMap = new HashMap<>();
        return overlayMap;
    }
}
