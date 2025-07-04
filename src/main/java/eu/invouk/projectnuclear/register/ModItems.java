package eu.invouk.projectnuclear.register;

import eu.invouk.projectnuclear.Projectnuclear;
import eu.invouk.projectnuclear.items.DebuggerItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Projectnuclear.MODID);

    public static final DeferredItem<Item> DEBUGGER_ITEM = ITEMS.registerItem("debugger", DebuggerItem::new);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
