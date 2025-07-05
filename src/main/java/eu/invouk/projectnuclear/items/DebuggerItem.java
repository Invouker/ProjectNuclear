package eu.invouk.projectnuclear.items;

import eu.invouk.projectnuclear.energynet.EnergyNet;
import eu.invouk.projectnuclear.energynet.IEnergyConsumer;
import eu.invouk.projectnuclear.energynet.IEnergyNode;
import eu.invouk.projectnuclear.energynet.IEnergyProducer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class DebuggerItem extends Item {

    public DebuggerItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if(context.getHand() != InteractionHand.MAIN_HAND) return InteractionResult.FAIL;
        BlockPos blockPos = context.getClickedPos();
        Level level = context.getLevel();
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        Player player = context.getPlayer();

        if(player == null) return InteractionResult.FAIL;
/*
        if (level.isClientSide()) { // Len na klientskej strane
            BlockPos clickedPos = context.getClickedPos();

            // Logika pridania/odobrania bloku
            if (BlockHighlightRenderer.getHighlightedBlocks().contains(clickedPos)) {
                BlockHighlightRenderer.removeBlockToHighlight(clickedPos);
                player.displayClientMessage(Component.literal("Block " + clickedPos + " removed from highlight."), false);
            } else {
                BlockHighlightRenderer.addBlockToHighlight(clickedPos);
                player.displayClientMessage(Component.literal("Block " + clickedPos + " added to highlight."), false);
            }
            return InteractionResult.SUCCESS;
        }*/

        if(!player.isShiftKeyDown())
            return InteractionResult.FAIL;

        if(blockEntity instanceof IEnergyNode energyNode ) {
            EnergyNet net = energyNode.getEnergyNet();
            if (net == null) {
                return InteractionResult.SUCCESS;
            }

            player.displayClientMessage(Component.literal("=== EnergyNet info ==="), false);
            player.displayClientMessage(Component.literal("Počet uzlov: " + net.getAllNodes().size()), false);
            player.displayClientMessage(Component.literal("Sieť validná: " + net.isValid()), false);
            player.displayClientMessage(Component.empty(),false);

            net.setHighlighted(!net.isHighlighted());

            for (IEnergyNode node : net.getAllNodes()) {

/*
                if (BlockHighlightRenderer.getHighlightedBlocks().contains(node.getPosition()))
                    BlockHighlightRenderer.removeBlockToHighlight(node.getPosition());
                 else
                    BlockHighlightRenderer.addBlockToHighlight(node.getPosition());*/

                String info = node.getClass().getSimpleName();
                if(node instanceof BlockEntity bufferTile)
                    player.displayClientMessage(Component.literal("# " + info + " @ Position - X:" + bufferTile.getBlockPos().getX() + " - Y:" + bufferTile.getBlockPos().getY() + " - Z:" + bufferTile.getBlockPos().getZ()),false);

                if(node instanceof IEnergyProducer producer) {
                    player.displayClientMessage(Component.literal("-P- Stored energy: " + producer.getEnergyStorage().getEnergyStored()), false);
                    player.displayClientMessage(Component.literal("-P- Max energy stored: " + producer.getEnergyStorage().getMaxEnergyStored()), false);
                } else if (node instanceof IEnergyConsumer consumer){
                    player.displayClientMessage(Component.literal("-C- Stored energy: " + consumer.getEnergyStorage().getEnergyStored()), false);
                    player.displayClientMessage(Component.literal("-C- Max energy stored: " + consumer.getEnergyStorage().getMaxEnergyStored()), false);
                    player.displayClientMessage(Component.literal("-C- Priority: " + consumer.getPriority()), false);
                    player.displayClientMessage(Component.literal("-C- Voltage: " + consumer.getVoltage()), false);
                    player.displayClientMessage(Component.literal("-C- IsAlive: " + consumer.isAlive()), false);
                } /*else if (node instanceof IEnergyCable cable){
                    player.displayClientMessage(Component.literal("-- Priority: " + cable.getVoltageRating()), false);
                    player.displayClientMessage(Component.literal("-- Capacity: " + cable.getCapacity()), false);
                }*/
                player.displayClientMessage(Component.empty(),false);
            }

            return InteractionResult.SUCCESS;
        }

        return super.useOn(context);
    }

    public void getInfoNetBlock(IEnergyNode node) {
    }
}
