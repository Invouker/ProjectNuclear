package eu.invouk.projectnuclear.gui.widgets;

import eu.invouk.projectnuclear.Projectnuclear;
import eu.invouk.projectnuclear.utils.Utils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class EnergyBarWidget extends AbstractWidget {

    private static final ResourceLocation ENERGY_BAR_TEXTURE = ResourceLocation.fromNamespaceAndPath(Projectnuclear.MODID, "textures/gui/widgets/energy_bar.png");

    private static final int BG_TEXTURE_X = 0;
    private static final int BG_TEXTURE_Y = 0;
    private static final int BG_WIDTH = 20;
    private static final int BG_HEIGHT = 50;

    private static final int EB_TEXTURE_X = 20;
    private static final int EB_TEXTURE_Y = 2;
    private static final int EB_WIDTH = 32;
    private static final int EB_HEIGHT = 45;

    private int energy;
    private int maxEnergy;

    private final Font font;

    private final IEnergyProvider iEnergyProvider;

    public EnergyBarWidget(Font font, int x, int y, IEnergyProvider iEnergyProvider) {
        super(x, y, BG_WIDTH, BG_HEIGHT, Component.empty());
        this.energy = 0;
        this.maxEnergy = 1;
        this.font = font;

        this.iEnergyProvider = iEnergyProvider;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(RenderType::guiTexturedOverlay, ENERGY_BAR_TEXTURE, this.getX(), this.getY(), BG_TEXTURE_X, BG_TEXTURE_Y, BG_WIDTH, BG_HEIGHT, 55, 50);

        energy = Math.max(0, iEnergyProvider.getEnergy());
        maxEnergy = Math.max(0, iEnergyProvider.getMaxEnergy());

        float energyPercent = Math.min(Math.max((float) energy / maxEnergy, 0f), 1f);
        int filledHeight = (int) (EB_HEIGHT * energyPercent);
        int fillX = this.getX() + (BG_WIDTH - EB_WIDTH) / 2;
        int fillY = this.getY() + BG_HEIGHT - filledHeight;

        if (filledHeight >= 0) {
            guiGraphics.blit(RenderType::guiTexturedOverlay, ENERGY_BAR_TEXTURE, fillX+8, fillY -2, EB_TEXTURE_X, EB_TEXTURE_Y + (EB_HEIGHT - filledHeight), EB_WIDTH, filledHeight, 53, 50);
        }

        if (this.isMouseOver(mouseX, mouseY)) {
            List<Component> tooltip = List.of(
                    Component.literal("Battery: " + Utils.format(energy) + " / " + Utils.format(maxEnergy) + " FE"),
                    Component.literal("Status: " + Math.round(energyPercent*100) + "% charged")
            );
            guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("widget.energybar", energy, maxEnergy));
    }
}
