package eu.invouk.projectnuclear.utils;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import static eu.invouk.projectnuclear.utils.Utils.getBurnTime;

public class BurnableItemStackHandler extends ItemStackHandler {

    public BurnableItemStackHandler(int size) {
        super(size);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        // Ak položka nie je spaľovateľná, vráť ju celú späť, neprijímame ju
        if (getBurnTime(stack) <= 0) {
            return stack;
        }
        // Inak vlož položku štandardne
        return super.insertItem(slot, stack, simulate);
    }
}
