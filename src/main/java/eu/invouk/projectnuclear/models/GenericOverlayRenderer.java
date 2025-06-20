package eu.invouk.projectnuclear.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;

public class GenericOverlayRenderer<T extends BlockEntity & IOverlayRenderable> implements BlockEntityRenderer<T> {

    public GenericOverlayRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@NotNull T tile, float partialTick, PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay, @NotNull Vec3 cameraPos) {
        poseStack.pushPose();
        Minecraft mc = Minecraft.getInstance();


        // Priprav atlas a base textúru
        Function<ResourceLocation, TextureAtlasSprite> atlas = mc.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS);
        //GpuTexture atlasTexture = mc.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).getTexture();

        // Rendering Block
        ResourceLocation machineTexture = tile.getMachineTexture();
        TextureAtlasSprite machineBaseSprite = atlas.apply(machineTexture);
        VertexConsumer machineBaseBuffer = bufferSource.getBuffer(RenderType.solid());

        for (Direction side : Direction.values()) {
            int blockLight = getSideLight(side, tile);
            renderFace(side, poseStack, machineBaseBuffer, blockLight, packedOverlay, machineBaseSprite, 0, false, false);
        }

        // Rendering overlays by MachineRenderer
        for (Direction side : Direction.values()) {
            MachineRenderer machineRenderer = tile.getSidedOverlay();
            Map<Direction, ResourceLocation> overlayMap = machineRenderer.build();
            if(!overlayMap.containsKey(side))
                continue;

            ResourceLocation overlayTexture = overlayMap.get(side);
            if (overlayTexture == null) continue;

            TextureAtlasSprite overlaySprite = atlas.apply(overlayTexture);
            if (overlaySprite == null) continue;

            boolean glow = tile.isOverlayGlowing(side);
            RenderType renderType = glow ? RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS) : RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS);

            VertexConsumer overlayBuffer = bufferSource.getBuffer(renderType);

            int blockLight = getSideLight(side, tile);
            renderFace(side, poseStack, overlayBuffer, blockLight, packedOverlay, overlaySprite, 0.002f, true, true);
        }
        poseStack.popPose();
    }


    private int getSideLight(Direction side, @NotNull T tile) {
        return LightTexture.pack(
                tile.getLevel().getBrightness(LightLayer.BLOCK, tile.getBlockPos().relative(side)),
                tile.getLevel().getBrightness(LightLayer.SKY, tile.getBlockPos().relative(side))
        );
    }

    public static void renderFace(Direction side, PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, TextureAtlasSprite sprite, float offset, boolean flipU, boolean flipV) {
        PoseStack.Pose pose = poseStack.last();
        float u0 = sprite.getU1(); // flipped!
        float u1 = sprite.getU0(); // flipped!
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        if (flipU) {
            float tmp = u0;
            u0 = u1;
            u1 = tmp;
        }
        if (flipV) {
            float tmp = v0;
            v0 = v1;
            v1 = tmp;
        }

        switch (side) {
            case NORTH -> addQuad(buffer, pose,
                    0, 0, 0 - offset,  0, 1, 0 - offset,  1, 1, 0 - offset,  1, 0, 0 - offset,
                    u0, v0, u1, v1, 0, 0, -1, packedLight, packedOverlay);
            case SOUTH -> addQuad(buffer, pose,
                    1, 0, 1 + offset,  1, 1, 1 + offset,  0, 1, 1 + offset,  0, 0, 1 + offset,
                    u0, v0, u1, v1, 0, 0, 1, packedLight, packedOverlay);
            case WEST -> addQuad(buffer, pose,
                    0 - offset, 0, 1,  0 - offset, 1, 1,  0 - offset, 1, 0,  0 - offset, 0, 0,
                    u0, v0, u1, v1, -1, 0, 0, packedLight, packedOverlay);
            case EAST -> addQuad(buffer, pose,
                    1 + offset, 0, 0,  1 + offset, 1, 0,  1 + offset, 1, 1,  1 + offset, 0, 1,
                    u0, v0, u1, v1, 1, 0, 0, packedLight, packedOverlay);
            case UP -> addQuad(buffer, pose,
                    0, 1 + offset, 0,  0, 1 + offset, 1,  1, 1 + offset, 1,  1, 1 + offset, 0,
                    u0, v0, u1, v1, 0, 1, 0, packedLight, packedOverlay);
            case DOWN -> addQuad(buffer, pose,
                    0, 0 - offset, 1,  0, 0 - offset, 0,  1, 0 - offset, 0,  1, 0 - offset, 1,
                    u0, v0, u1, v1, 0, -1, 0, packedLight, packedOverlay);
        }
    }

    private static void addQuad(VertexConsumer buffer, PoseStack.Pose pose,
                         float x0, float y0, float z0,
                         float x1, float y1, float z1,
                         float x2, float y2, float z2,
                         float x3, float y3, float z3,
                         float u0, float v0, float u1, float v1,
                         float nx, float ny, float nz,
                         int packedLight, int packedOverlay) {

        putVertex(buffer, pose, x0, y0, z0, u0, v0, nx, ny, nz, packedLight, packedOverlay);
        putVertex(buffer, pose, x1, y1, z1, u0, v1, nx, ny, nz, packedLight, packedOverlay);
        putVertex(buffer, pose, x2, y2, z2, u1, v1, nx, ny, nz, packedLight, packedOverlay);
        putVertex(buffer, pose, x3, y3, z3, u1, v0, nx, ny, nz, packedLight, packedOverlay);
    }


    private static void putVertex(VertexConsumer buffer, PoseStack.Pose pose,
                           float x, float y, float z,
                           float u, float v,
                           float nx, float ny, float nz,
                           int packedLight, int packedOverlay) {

        buffer.addVertex(pose.pose(), x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(packedOverlay)
                .setLight(packedLight)
                .setNormal(pose, nx, ny, nz);
    }
}
