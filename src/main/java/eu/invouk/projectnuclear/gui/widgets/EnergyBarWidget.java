package eu.invouk.projectnuclear.gui.widgets;

import eu.invouk.projectnuclear.Projectnuclear;
import eu.invouk.projectnuclear.utils.Utils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class EnergyBarWidget extends AbstractWidget {

    // Jeden sprite-sheet
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Projectnuclear.MODID, "textures/gui/widgets/energy_bar.png");

    // KONŠTANTY offsety
    private static final int BG_TEX_X = 0;
    private static final int BG_TEX_Y = 0;
    private static final int BG_WIDTH = 20;
    private static final int BG_HEIGHT = 42;

    private static final int EB_TEX_X = 40;
    private static final int EB_TEX_Y = 0;
    private static final int EB_WIDTH = 16;
    private static final int EB_HEIGHT = 38;

    private int energy;
    private int maxEnergy;

    private final Font font;

    private final IEnergyProvider iEnergyProvider;

    public EnergyBarWidget(Font font, int x, int y, IEnergyProvider iEnergyProvider) {
        // Výška a šírka pre widget sú podľa pozadia
        super(x, y, BG_WIDTH, BG_HEIGHT, Component.empty());
        this.energy = 0;
        this.maxEnergy = 1;  // ochrana proti deleniu nulou
        this.font = font;

        this.iEnergyProvider = iEnergyProvider;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 1️⃣ Vykresli pozadie
        guiGraphics.blit(
                RenderType::guiTexturedOverlay,
                TEXTURE,
                this.getX(), this.getY(),
                BG_TEX_X, BG_TEX_Y,
                BG_WIDTH, BG_HEIGHT,
                128, 128
        );

        energy = Math.max(0, iEnergyProvider.getEnergy());
        maxEnergy = Math.max(0, iEnergyProvider.getMaxEnergy());

        float energyPercent = Math.min(Math.max((float) energy / maxEnergy, 0f), 1f);
        int filledHeight = (int) (EB_HEIGHT * energyPercent);
        int fillY = this.getY() + BG_HEIGHT - filledHeight;
        int fillX = this.getX() + (BG_WIDTH - EB_WIDTH) / 2;

        if (filledHeight >= 0) {
            guiGraphics.blit(
                    RenderType::guiTexturedOverlay, // rovnaký typ ako pozadie
                    TEXTURE,
                    fillX, fillY -1,
                    EB_TEX_X, EB_TEX_Y + (EB_HEIGHT - filledHeight),
                    EB_WIDTH+1, filledHeight,
                    128, 128
            );
        }

        // 3️⃣ Tooltip
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
        narrationElementOutput.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE,
                Component.translatable("widget.energybar", energy, maxEnergy));
    }
}
