package eu.invouk.projectnuclear.register;

import eu.invouk.projectnuclear.Projectnuclear;
import eu.invouk.projectnuclear.tile.BasicBatteryBufferTile;
import eu.invouk.projectnuclear.tile.BasicCableTile;
import eu.invouk.projectnuclear.tile.CoalGeneratorTile;
import eu.invouk.projectnuclear.tile.TransformerBlockTile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocksEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Projectnuclear.MODID);

    public static final Supplier<BlockEntityType<CoalGeneratorTile>> COAL_GENERATOR_TILE =
            BLOCK_ENTITIES.register("coal_generator_tile",
                    () -> new BlockEntityType<>(CoalGeneratorTile::new,
                            false,
                    ModBlocks.COAL_GENERATOR.get()));

    public static final Supplier<BlockEntityType<BasicCableTile>> BASIC_CABLE_TILE =
            BLOCK_ENTITIES.register("basic_cable_tile",
                    () -> new BlockEntityType<>(BasicCableTile::new,
                            false,
                    ModBlocks.BASIC_CABLE.get()));

    public static final Supplier<BlockEntityType<BasicBatteryBufferTile>> BASIC_BATTERY_BUFFER =
            BLOCK_ENTITIES.register("basic_battery_buffer",
                    () -> new BlockEntityType<>(BasicBatteryBufferTile::new,
                            false,
                    ModBlocks.BASIC_BATTERY_BUFFER.get()));

    public static final Supplier<BlockEntityType<TransformerBlockTile>> TRANSFORMER_BLOCK_TILE =
            BLOCK_ENTITIES.register("transformer_block_tile",
                    () -> new BlockEntityType<>(TransformerBlockTile::new,
                            false,
                    ModBlocks.TRANSFORMER_BLOCK.get()));



    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }

}
