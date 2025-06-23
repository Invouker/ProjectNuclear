package eu.invouk.projectnuclear.gui.widgets;

import eu.invouk.projectnuclear.Projectnuclear;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ProgressBarWidget extends AbstractWidget {

    private static final ResourceLocation ARROW_FULL = ResourceLocation.fromNamespaceAndPath(Projectnuclear.MODID, "textures/gui/widgets/progress_arrow_full.png");
    private static final ResourceLocation ARROW_EMPTY = ResourceLocation.fromNamespaceAndPath(Projectnuclear.MODID, "textures/gui/widgets/progress_arrow_empty.png");

    private final IProgressProvider progress;

    public ProgressBarWidget(int x, int y, IProgressProvider progress) {
        super(x, y, 22, 16, Component.empty());
        this.progress = progress;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {
        guiGraphics.blit(RenderType::guiTexturedOverlay, ARROW_EMPTY, getX(), getY(), 0, 0, this.width,this.height,this.width, this.height);

        int fillWidth = (int) (this.width * (progress.getProgress() / 100.0f));
        if (fillWidth > 0)
            guiGraphics.blit(RenderType::guiTexturedOverlay, ARROW_FULL, getX(), getY(), 0, 0, width-fillWidth, this.height, this.width, this.height);
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}
}
