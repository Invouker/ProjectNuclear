package eu.invouk.projectnuclear.blocks;

import com.mojang.serialization.MapCodec;
import eu.invouk.projectnuclear.energynet.EnergyNetManager;
import eu.invouk.projectnuclear.energynet.IEnergyNode;
import eu.invouk.projectnuclear.register.ModBlocksEntities;
import eu.invouk.projectnuclear.tile.CoalGeneratorTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;


public class CoalGenerator extends BaseEntityBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    //private final MapCodec<CoalGenerator> codec = MapCod

    public CoalGenerator(BlockBehaviour.Properties properties) {
        super(properties
                .destroyTime(2f)
                .requiresCorrectToolForDrops()
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CoalGeneratorTile(blockPos, blockState);
    }

    @Override
    public  <T extends BlockEntity> BlockEntityTicker getTicker(Level level, BlockState blockState, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlocksEntities.COAL_GENERATOR_TILE.get(), CoalGeneratorTile::tick);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof MenuProvider provider) {
                if(player instanceof ServerPlayer sPlayer && !player.isShiftKeyDown())
                    sPlayer.openMenu(provider);

            }
            if(player.isShiftKeyDown()) {
                if (blockEntity instanceof IEnergyNode energyNode) {
                    player.displayClientMessage(Component.literal("[Debug] " + energyNode.getDebugInfo()), false);
                } else {
                    player.displayClientMessage(Component.literal("[Debug] Not an energy node"), false);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, LevelAccessor level, BlockPos pos, Rotation direction) {
        return state.setValue(FACING, direction.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
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
