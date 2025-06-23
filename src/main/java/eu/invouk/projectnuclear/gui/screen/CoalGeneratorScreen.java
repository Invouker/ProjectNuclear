package eu.invouk.projectnuclear.gui.screen;

import eu.invouk.projectnuclear.gui.menu.CoalGeneratorMenu;
import eu.invouk.projectnuclear.gui.widgets.EnergyBarWidget;
import eu.invouk.projectnuclear.gui.widgets.IEnergyProvider;
import eu.invouk.projectnuclear.gui.widgets.ProgressBarWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CoalGeneratorScreen extends GenericContainerScreen<CoalGeneratorMenu> {

    public CoalGeneratorScreen(CoalGeneratorMenu coalGeneratorMenu, Inventory inventory, Component component) {
        super(coalGeneratorMenu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();

        ProgressBarWidget progressBarWidget = new ProgressBarWidget(centerX - 10, centerY - 65, () -> menu.getData().get(2));
        EnergyBarWidget energyBarWidget = new EnergyBarWidget(this.font, centerX + 50,centerY - 77, new IEnergyProvider() {
             @Override
             public int getEnergy() {
                 return menu.getData().get(0);
             }

             @Override
             public int getMaxEnergy() {
                 return menu.getData().get(1);
             }
         });

         registerWidget(progressBarWidget);
         registerWidget(energyBarWidget);
    }
}
