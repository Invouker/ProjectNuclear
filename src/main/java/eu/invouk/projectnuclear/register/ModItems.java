package eu.invouk.projectnuclear.register;

import eu.invouk.projectnuclear.Projectnuclear;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Projectnuclear.MODID);

    //public static final DeferredItem<Item> COAL_GENERATOR_ITEM_BLOCK = ITEMS.registerSimpleItem("coal_generator.jsonss");

    //public static final DeferredItem<Item> test = ITEMS.registerSimpleItem("Aha");



    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
