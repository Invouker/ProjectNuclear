package eu.invouk.projectnuclear.blocks.energy;

import com.mojang.serialization.MapCodec;
import eu.invouk.projectnuclear.energynet.EnergyNetManager;
import eu.invouk.projectnuclear.energynet.IEnergyCable;
import eu.invouk.projectnuclear.energynet.IEnergyNode;
import eu.invouk.projectnuclear.packets.C2S.CableDebugPayload;
import eu.invouk.projectnuclear.tile.BasicCableTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class BasicCable extends BaseEntityBlock {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    protected static final VoxelShape CORE_SHAPE = Block.box(6.0, 6.0, 6.0, 10.0, 10.0, 10.0);
    protected static final VoxelShape NORTH_SHAPE = Block.box(6.0, 6.0, 0.0, 10.0, 10.0, 6.0);
    protected static final VoxelShape SOUTH_SHAPE = Block.box(6.0, 6.0, 10.0, 10.0, 10.0, 16.0);
    protected static final VoxelShape WEST_SHAPE = Block.box(0.0, 6.0, 6.0, 6.0, 10.0, 10.0);
    protected static final VoxelShape EAST_SHAPE = Block.box(10.0, 6.0, 6.0, 16.0, 10.0, 10.0);
    protected static final VoxelShape DOWN_SHAPE = Block.box(6.0, 0.0, 6.0, 10.0, 6.0, 10.0);
    protected static final VoxelShape UP_SHAPE = Block.box(6.0, 10.0, 6.0, 10.0, 16.0, 10.0);


    public BasicCable(Properties properties) {
        super(properties.noOcclusion().dynamicShape());

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BasicCableTile(blockPos, blockState);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IEnergyNode node) {
            EnergyNetManager.register(node);
        }

        BlockState currentState = state; // Začni s pôvodným stavom
        RandomSource random = RandomSource.create(); // Vytvor si inštanciu RandomSource

        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);

            // Volanie požadovanej updateShape metódy
            // Poznámka: scheduledTickAccess je null, čo môže mať vplyv, ak sa metóda spolieha na plánované ticky.
            currentState = this.updateShape(currentState, level, null, pos, direction, neighborPos, neighborState, random);
        }
        // Ak sa stav zmenil, aktualizuj ho vo svete
        if (currentState != state) {
            level.setBlock(pos, currentState, 3); // Flag 3 = aktualizovať susedov a renderovanie
        }

    }

    @Override
    protected int getLightBlock(BlockState state) {
        return 0;
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IEnergyNode node) {
            EnergyNetManager.unregister(node);
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;  // nechaj client-side len na animáciu

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IEnergyCable cable) {
            CableDebugPayload payload = new CableDebugPayload(pos.asLong(), 0);
            PacketDistributor.sendToServer(payload);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (level.isClientSide()) return state;

        boolean connected = false;
        BlockEntity neighborBE = level.getBlockEntity(neighborPos);

        if (neighborBE instanceof IEnergyNode) {
            connected = true;
        } else if (neighborBE != null && level.getBlockState(neighborPos).hasBlockEntity()) {
            // Skontroluj, či BlockEntity v susednom bloku môže poskytovať/prijímať energiu
            // Tu by si mal použiť NeoForge Capabilities
            // Napr. if (neighborBE.getCapability(Capabilities.Energy.BLOCK, direction.getOpposite()).isPresent()) {
            //           connected = true;
            //       }
            // Pre jednoduchosť zatiaľ postačí IEnergyNode
        }

        switch (direction) {
            case NORTH:
                return state.setValue(NORTH, connected);
            case SOUTH:
                return state.setValue(SOUTH, connected);
            case EAST:
                return state.setValue(EAST, connected);
            case WEST:
                return state.setValue(WEST, connected);
            case UP:
                return state.setValue(UP, connected);
            case DOWN:
                return state.setValue(DOWN, connected);
            default:
                return state;
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Začni s jadrom kábla
        VoxelShape shape = CORE_SHAPE;

        // Podľa BlockState pridaj pripojenia
        if (state.getValue(NORTH)) {
            shape = Shapes.or(shape, NORTH_SHAPE);
        }
        if (state.getValue(SOUTH)) {
            shape = Shapes.or(shape, SOUTH_SHAPE);
        }
        if (state.getValue(EAST)) {
            shape = Shapes.or(shape, EAST_SHAPE);
        }
        if (state.getValue(WEST)) {
            shape = Shapes.or(shape, WEST_SHAPE);
        }
        if (state.getValue(UP)) {
            shape = Shapes.or(shape, UP_SHAPE);
        }
        if (state.getValue(DOWN)) {
            shape = Shapes.or(shape, DOWN_SHAPE);
        }

        return shape;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state) {
        return Shapes.empty();
    }


}
