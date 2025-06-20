package eu.invouk.projectnuclear.gui.screen;

import eu.invouk.projectnuclear.Projectnuclear;
import eu.invouk.projectnuclear.gui.menu.CoalGeneratorMenu;
import eu.invouk.projectnuclear.gui.widgets.EnergyBarWidget;
import eu.invouk.projectnuclear.gui.widgets.IEnergyProvider;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CoalGeneratorScreen extends AbstractContainerScreen<CoalGeneratorMenu> {

    private static final ResourceLocation COAL_GENERATOR_TEXTURE = ResourceLocation.fromNamespaceAndPath(Projectnuclear.MODID, "textures/gui/coal_generator_gui.png");

    private EnergyBarWidget energyBarWidget;
    public CoalGeneratorScreen(CoalGeneratorMenu coalGeneratorMenu, Inventory inventory, Component component) {
        super(coalGeneratorMenu, inventory, component);

        this.imageWidth = 176;
        this.imageHeight = 201;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;


         energyBarWidget = new EnergyBarWidget(this.font, (this.width / 2) + 50,(this.height/2) - 77, new IEnergyProvider() {
             @Override
             public int getEnergy() {
                 return menu.getData().get(0);
             }

             @Override
             public int getMaxEnergy() {
                 return menu.getData().get(1);
             }
         });

    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(RenderType::guiTextured, COAL_GENERATOR_TEXTURE, x, y, 0,0, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        energyBarWidget.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

}
