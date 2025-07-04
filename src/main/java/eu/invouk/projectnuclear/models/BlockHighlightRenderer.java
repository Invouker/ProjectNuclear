package eu.invouk.projectnuclear.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import eu.invouk.projectnuclear.Projectnuclear;
import eu.invouk.projectnuclear.energynet.IEnergyCable;
import eu.invouk.projectnuclear.energynet.IEnergyProducer;
import eu.invouk.projectnuclear.items.DebuggerItem;
import eu.invouk.projectnuclear.tile.BasicBatteryBufferTile;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.*;


@EventBusSubscriber(modid = Projectnuclear.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class BlockHighlightRenderer {

    private static final Set<BlockPos> HIGHLIGHTED_BLOCKS = Collections.synchronizedSet(new HashSet<>());

    public static void addBlockToHighlight(BlockPos pos) {
        HIGHLIGHTED_BLOCKS.add(pos.immutable());
    }

    public static void removeBlockToHighlight(BlockPos pos) {
        HIGHLIGHTED_BLOCKS.remove(pos);
    }

    public static Set<BlockPos> getHighlightedBlocks() {
        return HIGHLIGHTED_BLOCKS;
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;


        //if(event.getLevel().isClientSide()) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        Camera camera = event.getCamera();
        Minecraft minecraft = Minecraft.getInstance();

        if(minecraft.player != null && !(minecraft.player.getMainHandItem().getItem() instanceof DebuggerItem)) return;
        if(HIGHLIGHTED_BLOCKS == null || getHighlightedBlocks().isEmpty()) return;


       synchronized (getHighlightedBlocks()) {
           for (BlockPos blockPos : getHighlightedBlocks()) {
               Level level = event.getLevel();
               BlockEntity blockEntity = level.getBlockEntity(blockPos) ;
               if(blockEntity == null) continue;
               List<String> Text3D = getNodeInfo(blockPos, blockEntity);

               render3DText(Text3D, blockPos, poseStack, bufferSource, camera);
               renderBlockHighlight(poseStack, bufferSource, blockPos, 1f, 1f, 1f, 0.5f);
           }
       }
    }

    private static @NotNull List<String> getNodeInfo(BlockPos blockPos, BlockEntity blockEntity) {
        String simpleName = blockEntity.getClass().getSimpleName();

        List<String> Text3D = new ArrayList<>(List.of("§e" + simpleName, blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ(), "", "§aEnergyNode"));
        if(blockEntity instanceof IEnergyCable cable) {
            Text3D.add("Capacity: " + cable.getCapacity());
            Text3D.add("Voltage: " + cable.getVoltageRating());
        }
        else if(blockEntity instanceof IEnergyProducer producer) {
            Text3D.add("P: Capacity: " + producer.getEnergyStorage().getEnergyStored() + "/" + producer.getEnergyStorage().getMaxEnergyStored());
            Text3D.add("Voltage: " + producer.getVoltage());
        }
        else if(blockEntity instanceof BasicBatteryBufferTile consumer) {
            Text3D.add("C: Capacity: " + consumer.getEnergyStorage().getEnergyStored() + "/" + consumer.getEnergyStorage().getMaxEnergyStored());
            Text3D.add("Voltage: " + consumer.getVoltage());
            Text3D.add("Alive: " + consumer.isAlive());
            Text3D.add("Priority: " + consumer.getPriority());
        }
        return Text3D;
    }

    private static void render3DText(List<String> text, BlockPos blockPos, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Camera camera) {
        Font font = Minecraft.getInstance().font;

        double textX = blockPos.getCenter().x;
        double textY = blockPos.getY()+1.85;
        double textZ = blockPos.getCenter().z;

        Vec3 camPos = camera.getPosition();
        poseStack.pushPose();
        poseStack.translate(textX - camPos.x, textY - camPos.y, textZ - camPos.z);

        poseStack.mulPose(camera.rotation());
        poseStack.mulPose(new Quaternionf().rotationY((float)Math.toRadians(180d)).setAngleAxis((float) Math.toRadians(180.0F), 0.0F, 1.0F, 0.0F));
        float scale = 0.008f;
        poseStack.scale(-scale, -scale, scale);

        int alpha = (int)(0.3f * 255);  // 77
        int color = (alpha << 24) | 0x00595959;
        int lineHeight = font.lineHeight;

        for (int i = 0; i <text.size(); i++) {
            String string = text.get(i);
            Component textComponent = Component.literal(string);

            int textWidth = font.width(textComponent);
            float offsetX = -textWidth / 2f;
            float offsetY = i * lineHeight;

            font.drawInBatch(textComponent, offsetX, offsetY, 0xFFFFFFFF, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
            font.drawInBatch(textComponent, offsetX, offsetY, 0xFFFFFFFF, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, color, 15728880);
        }
        poseStack.popPose();
        bufferSource.endBatch();
    }

    private static void renderBlockHighlight(PoseStack poseStack, MultiBufferSource bufferSource, BlockPos pos, float red, float green, float blue, float alpha) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());
        Matrix4f matrix = poseStack.last().pose();

        float x = pos.getX();
        float y = pos.getY();
        float z = pos.getZ();

        float[][] vertices = new float[][] {
                {x, y, z},
                {x + 1, y, z},
                {x + 1, y, z + 1},
                {x, y, z + 1},
                {x, y + 1, z},
                {x + 1, y + 1, z},
                {x + 1, y + 1, z + 1},
                {x, y + 1, z + 1}
        };

        int[][] edges = new int[][] {
                {0, 1}, {1, 2}, {2, 3}, {3, 0},
                {4, 5}, {5, 6}, {6, 7}, {7, 4},
                {0, 4}, {1, 5}, {2, 6}, {3, 7}
        };

        int overlay = 0;
        int light = 0xF000F0;

        for (int[] edge : edges) {
            float[] start = vertices[edge[0]];
            float[] end = vertices[edge[1]];

            vertexConsumer.addVertex(matrix, start[0], start[1], start[2])
                    .setColor(red, green, blue, alpha)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal(0, 1, 0);

            vertexConsumer.addVertex(matrix, end[0], end[1], end[2])
                    .setColor(red, green, blue, alpha)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal(0, 1, 0);
        }
    }





       /*
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        poseStack.pushPose();
        render3DText(poseStack, bufferSource, event.getCamera());
/*
        double camX = event.getCamera().getPosition().x;
        double camY = event.getCamera().getPosition().y;
        double camZ = event.getCamera().getPosition().z;

        poseStack.pushPose();
        poseStack.translate(-camX, -camY, -camZ);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        for (BlockPos pos : HIGHLIGHTED_BLOCKS) {
            renderBlockHighlight(poseStack, bufferSource, pos, 1f, 0f, 0f, 0.5f);
            renderBlockText(poseStack, pos, bufferSource, event.getCamera());
        }

        poseStack.popPose();

        // END BATCH len tu, raz
        bufferSource.endBatch();
    }*/

}