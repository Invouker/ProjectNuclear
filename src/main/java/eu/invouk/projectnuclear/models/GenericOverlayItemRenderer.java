package eu.invouk.projectnuclear.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.invouk.projectnuclear.Projectnuclear;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;


public record GenericOverlayItemRenderer(String itemName, ResourceLocation texture) implements NoDataSpecialModelRenderer {

    @Override
    public void render(@NotNull ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, boolean hasFoil) {
        poseStack.pushPose();

        // Výchozí scale
        float scale = 1.0f;
        switch (displayContext) {
            case GUI -> {
                poseStack.translate(0.38F, -0.18, 0.0F);
                poseStack.scale(0.6F, 0.6F, 0.6F);
            }
            case GROUND -> {
                poseStack.translate(0.7, 0, 0.4);
                poseStack.scale(0.65F, 0.65F, 0.65F);
            }
            case FIXED -> {
                poseStack.translate(0.0F, 0.0F, 0.0F);
                poseStack.scale(0.5F, 0.5F, 0.5F);
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                poseStack.translate(0.0F, 0.25F, 0.0F);
                poseStack.scale(0.775F, 0.775F, 0.775F);
            }
            case FIRST_PERSON_LEFT_HAND -> {
                poseStack.translate(0.0F, 0.25F, 0.0F);
                poseStack.scale(0.5F, 0.5F, 0.5F);
            }
            case FIRST_PERSON_RIGHT_HAND -> {
                poseStack.translate(0.35F, 0.25F, 0.0F);
                poseStack.scale(0.5F, 0.5F, 0.5F);
            }
            default -> {
                poseStack.scale(1.0F, 1.0F, 1.0F);
            }
        }

        Minecraft minecraft = Minecraft.getInstance();
        Function<ResourceLocation, TextureAtlasSprite> atlas = minecraft.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite sprite = atlas.apply(texture);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));

        for (var side : Direction.values()) {
            GenericOverlayRenderer.renderFace(side, poseStack, buffer, packedLight, packedOverlay, sprite, 0, false, false);
        }

        {
            TextureAtlasSprite faceSprite = atlas.apply(ResourceLocation.fromNamespaceAndPath(Projectnuclear.MODID, "block/" + itemName));
            VertexConsumer faceBuffer = bufferSource.getBuffer(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));

            GenericOverlayRenderer.renderFace(Direction.SOUTH, poseStack, faceBuffer, packedLight, packedOverlay, faceSprite, 0, true, true);
        }

        poseStack.translate(0, 0, 0);
        poseStack.scale(1, 1, 1);


/*
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

            addVertexConsumer overlayBuffer = bufferSource.getBuffer(renderType);
            renderFace(side, poseStack, overlayBuffer, 0xF000F0, packedOverlay, overlaySprite, 0.002f, true, true);
        }*/

        poseStack.popPose();
    }

    public record Unbaked(String itemName, ResourceLocation texture) implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<GenericOverlayItemRenderer.Unbaked> MAP_CODEC =
                RecordCodecBuilder.mapCodec(instance -> instance.group(
                        Codec.STRING.fieldOf("front_side_texture").forGetter(Unbaked::getItemName),
                        ResourceLocation.CODEC.fieldOf("texture").forGetter(Unbaked::getTexture)
                ).apply(instance, Unbaked::new));


        public Unbaked(ResourceLocation texture) {
            this(null, texture);
        }

        public ResourceLocation getTexture() {
            return texture;
        }

        public String getItemName() {
            return itemName;
        }

        @Override
        public SpecialModelRenderer<?> bake(@NotNull EntityModelSet modelSet) {
            return new GenericOverlayItemRenderer(itemName, texture);
        }

        @Override
        public @NotNull MapCodec<? extends SpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }

    /*
     public record Unbaked(String itemName, ResourceLocation texture) implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<GenericOverlayItemRenderer.Unbaked> MAP_CODEC = ResourceLocation.CODEC.fieldOf("texture")
                .xmap(GenericOverlayItemRenderer.Unbaked::new, GenericOverlayItemRenderer.Unbaked::texture);

        @Override
        public SpecialModelRenderer<?> bake(@NotNull EntityModelSet modelSet) {
            return new GenericOverlayItemRenderer(itemName, texture);
        }

        @Override
        public @NotNull MapCodec<? extends SpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }
    * */
}
