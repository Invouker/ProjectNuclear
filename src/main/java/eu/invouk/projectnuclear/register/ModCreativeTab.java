package eu.invouk.projectnuclear.register;

import eu.invouk.projectnuclear.Projectnuclear;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTab {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Projectnuclear.MODID);


    public static final Supplier<CreativeModeTab> PROJECT_NUCLEAR_TAB = CREATIVE_MODE_TAB.register("project_nuclear_tab", () -> CreativeModeTab.builder()
            .icon(() -> new ItemStack(ModBlocks.COAL_GENERATOR.asItem()))
            .title(Component.translatable("creativetab.projectnuclear.tab"))
            .displayItems((itemDisplayParameters, output) -> {
                output.accept(ModItems.DEBUGGER_ITEM);
                output.accept(ModBlocks.BASIC_CABLE);
                output.accept(ModBlocks.COAL_GENERATOR);
                output.accept(ModBlocks.BASIC_BATTERY_BUFFER);
                output.accept(ModBlocks.TRANSFORMER_BLOCK);
            }).build());

    public static void register(IEventBus modBus) {
        CREATIVE_MODE_TAB.register(modBus);
    }
}
