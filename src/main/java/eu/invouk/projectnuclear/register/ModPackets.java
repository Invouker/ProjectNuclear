package eu.invouk.projectnuclear.register;

import eu.invouk.projectnuclear.energynet.IEnergyCable;
import eu.invouk.projectnuclear.packets.C2S.CableDebugPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ModPackets {

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1").executesOn(HandlerThread.NETWORK);

        registrar.playBidirectional(
                CableDebugPayload.TYPE,
                CableDebugPayload.CODEC,
                new DirectionalPayloadHandler<>(
                        ModPackets::handleCableDebugClient,
                        ModPackets::handleCableDebugServer
                )
        );

    }

    private static void handleCableDebugClient(CableDebugPayload payload, IPayloadContext context) {
        context.enqueueWork(() ->
                Minecraft.getInstance().execute(() ->
                        Minecraft.getInstance().player.displayClientMessage(
                Component.literal("Cable at " + BlockPos.of(payload.pos()) + " transferred " + payload.transferredEnergy() + " V/t"),
                false
        )));
    }

    private static void handleCableDebugServer(CableDebugPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (player == null) return;

            ServerLevel level = (ServerLevel) player.level();
            BlockEntity be = level.getBlockEntity(BlockPos.of(payload.pos()));
            if (be instanceof IEnergyCable cable) {
                int transferred = cable.getEnergyTransferredThisTick();

                // Pošli späť odpoveď klientovi s hodnotou
                CableDebugPayload response = new CableDebugPayload(payload.pos(), transferred);
                PacketDistributor.sendToPlayer(player, response);
            }
        });
    }
}
