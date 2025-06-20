package eu.invouk.projectnuclear.models;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface IOverlayRenderable {
    @Nullable ResourceLocation getMachineTexture();
    default boolean isOverlayGlowing(Direction side) { return false; }
    @Nullable MachineRenderer getSidedOverlay();
}