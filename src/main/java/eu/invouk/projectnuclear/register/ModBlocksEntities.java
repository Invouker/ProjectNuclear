package eu.invouk.projectnuclear.register;

import eu.invouk.projectnuclear.Projectnuclear;
import eu.invouk.projectnuclear.tile.CoalGeneratorTile;
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



    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }

}
