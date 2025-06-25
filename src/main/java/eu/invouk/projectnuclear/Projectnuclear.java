package eu.invouk.projectnuclear;

import com.mojang.logging.LogUtils;
import eu.invouk.projectnuclear.energynet.EnergyNetManager;
import eu.invouk.projectnuclear.gui.screen.CoalGeneratorScreen;
import eu.invouk.projectnuclear.models.GenericOverlayItemRenderer;
import eu.invouk.projectnuclear.models.GenericOverlayRenderer;
import eu.invouk.projectnuclear.register.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialBlockModelRendererEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.slf4j.Logger;

@Mod(Projectnuclear.MODID)
public class Projectnuclear {
    public static final String MODID = "projectnuclear";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Projectnuclear(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(Projectnuclear::onWorldTick);

        ModItems.register(modBus);
        ModBlocks.register(modBus);
        ModCreativeTab.register(modBus);
        ModBlocksEntities.register(modBus);
        ModMenuTypes.register(modBus);
        new ModCapabilities(modBus);

    }

    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Pre event) {
        if (!event.getLevel().isClientSide()) {
            EnergyNetManager.tick(event.getLevel());
        }
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class Client {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            BlockEntityRenderers.register(ModBlocksEntities.COAL_GENERATOR_TILE.get(), GenericOverlayRenderer::new);

        }

        @SubscribeEvent
        public static void registerSpecialRenderers(RegisterSpecialModelRendererEvent event) {
            ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(MODID, "generic_overlay");
            event.register(resourceLocation, GenericOverlayItemRenderer.Unbaked.MAP_CODEC);
        }

        @SubscribeEvent
        public static void registerSpecialBlockRenderers(RegisterSpecialBlockModelRendererEvent event) {
            event.register(
                    ModBlocks.COAL_GENERATOR.get(),
                    new GenericOverlayItemRenderer.Unbaked(ResourceLocation.fromNamespaceAndPath(MODID, "block/machine_casing"))
            );
        }

        @SubscribeEvent
        public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.COAL_GENERATOR_MENU.get(), CoalGeneratorScreen::new);
        }

    }
}
