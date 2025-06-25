package eu.invouk.projectnuclear.blocks.energy;

import com.mojang.serialization.MapCodec;
import eu.invouk.projectnuclear.energynet.*;
import eu.invouk.projectnuclear.tile.BasicBatteryBufferTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicBatteryBuffer extends BaseEntityBlock {

    private static final Logger log = LoggerFactory.getLogger(BasicBatteryBuffer.class);

    public BasicBatteryBuffer(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BasicBatteryBufferTile(blockPos, blockState);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide && player.isShiftKeyDown()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof IEnergyNode energyNode) {
                player.displayClientMessage(Component.literal("[Debug] " + energyNode.getDebugInfo()), false);
            } else {
                player.displayClientMessage(Component.literal("[Debug] Not an energy node"), false);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IEnergyNode node) {
            EnergyNetManager.register(node);
        }
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        super.destroy(level, pos, state);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IEnergyNode node) {
            EnergyNetManager.unregister(node);
        }
    }

}
