package eu.invouk.projectnuclear.register;

import eu.invouk.projectnuclear.Projectnuclear;
import eu.invouk.projectnuclear.blocks.CoalGenerator;
import eu.invouk.projectnuclear.blocks.energy.BasicBatteryBuffer;
import eu.invouk.projectnuclear.blocks.energy.BasicCable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Projectnuclear.MODID);

    public static final DeferredBlock<Block> COAL_GENERATOR = registerBlock("coal_generator", CoalGenerator::new);
    public static final DeferredBlock<Block> BASIC_CABLE = registerBlock("basic_cable", BasicCable::new);
    public static final DeferredBlock<Block> BASIC_BATTERY_BUFFER = registerBlock("basic_battery_buffer", BasicBatteryBuffer::new);

    private static DeferredBlock<Block> registerBlock(String name, Block block) {
        return registerBlock(name, (properties -> block));
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> function) {
        DeferredBlock<T> toReturn = BLOCKS.registerBlock(name, function);
        registerBlockItem(name, toReturn);
        return toReturn; //t
    }


    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.registerItem(name, (properties) -> new BlockItem(block.get(), properties.useBlockDescriptionPrefix()));
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
