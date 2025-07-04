package eu.invouk.projectnuclear.blocks.energy;

import com.mojang.serialization.MapCodec;
import eu.invouk.projectnuclear.energynet.EnergyNetManager;
import eu.invouk.projectnuclear.energynet.IEnergyNode;
import eu.invouk.projectnuclear.tile.BasicCableTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicCable extends BaseEntityBlock {

    private static final Logger log = LoggerFactory.getLogger(BasicCable.class);

    public BasicCable(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BasicCableTile(blockPos, blockState);
    }
/*
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(hand != InteractionHand.MAIN_HAND) return InteractionResult.FAIL;
        if (!level.isClientSide && player.isShiftKeyDown()) {

            player.displayClientMessage(Component.literal("---------------------------------------"), false);
            player.displayClientMessage(Component.empty(),false);

            BlockEntity blockEntity = level.getBlockEntity(pos);
            IEnergyNode energyNode = (IEnergyNode) blockEntity;
            EnergyNet net = energyNode.getEnergyNet();
            if (net == null) {
                player.displayClientMessage(Component.literal("Tento blok nie je pripojený k EnergyNetu."), false);
                return InteractionResult.SUCCESS;
            }
            player.displayClientMessage(Component.literal("=== EnergyNet info ==="), false);
            player.displayClientMessage(Component.literal("== ## Počet uzlov: " + net.getAllNodes().size()), false);
            player.displayClientMessage(Component.literal("== ## Sieť validná: " + net.isValid()), false);
            player.displayClientMessage(Component.empty(),false);
            for (IEnergyNode node : net.getAllNodes()) {
                String info = String.format("== ## %s @ ", node.getClass().getSimpleName());
                player.displayClientMessage(Component.literal(info),false);
                if(node instanceof BlockEntity bufferTile) {
                    player.displayClientMessage(Component.literal("## Position @ - X:" + bufferTile.getBlockPos().getX() + ", Y: " + bufferTile.getBlockPos().getY() + ", Z: " + bufferTile.getBlockPos().getZ()),false);
                }
                if(node instanceof IEnergyProducer producer) {
                    player.displayClientMessage(Component.literal("-- Stored energy: " + producer.getEnergyStorage().getEnergyStored()), false);
                    player.displayClientMessage(Component.literal("-- Max energy stored: " + producer.getEnergyStorage().getMaxEnergyStored()), false);
                    player.displayClientMessage(Component.empty(),false);
                    if(producer instanceof CoalGeneratorTile bufferTile) {
                        player.displayClientMessage(Component.empty(),false);
                        player.displayClientMessage(Component.literal("-- Voltage: " + bufferTile.getVoltage()),false);
                    }
                } else if (node instanceof IEnergyConsumer consumer){
                    player.displayClientMessage(Component.literal(info),false);
                    player.displayClientMessage(Component.literal("-- Priority: " + consumer.getPriority()), false);
                    player.displayClientMessage(Component.literal("-- Voltage: " + consumer.getVoltage()), false);
                    player.displayClientMessage(Component.literal("-- IsAlive: " + consumer.isAlive()), false);
                    player.displayClientMessage(Component.empty(),false);
                } else if (node instanceof IEnergyCable cable){
                    player.displayClientMessage(Component.literal("-- Priority: " + cable.getVoltageRating()),false);
                    player.displayClientMessage(Component.literal("-- Capacity: " + cable.getCapacity()),false);
                    player.displayClientMessage(Component.empty(),false);
                }
            }
            player.displayClientMessage(Component.empty(),false);
            player.displayClientMessage(Component.literal("---------------------------------------"), false);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
*/

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IEnergyNode node) {
            EnergyNetManager.register(node);
        }
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IEnergyNode node) {
            EnergyNetManager.unregister(node);
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }


}
