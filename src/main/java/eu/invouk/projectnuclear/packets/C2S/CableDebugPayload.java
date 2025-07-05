package eu.invouk.projectnuclear.packets.C2S;

import eu.invouk.projectnuclear.Projectnuclear;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CableDebugPayload(Long pos, int transferredEnergy) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Projectnuclear.MODID, "cable_debug");

    public static final StreamCodec<FriendlyByteBuf, CableDebugPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.LONG,
            CableDebugPayload::pos,
            ByteBufCodecs.INT,
            CableDebugPayload::transferredEnergy,
            CableDebugPayload::new
    );

    public static final CustomPacketPayload.Type<CableDebugPayload> TYPE = new CustomPacketPayload.Type<>(ID);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}