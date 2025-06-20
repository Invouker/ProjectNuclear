package eu.invouk.projectnuclear.utils;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class Utils {

    public static int getBurnTime(@NotNull ItemStack stack) {
        Holder<Item> holder = stack.getItem().builtInRegistryHolder();

        FurnaceFuel fuel = holder.getData(NeoForgeDataMaps.FURNACE_FUELS);
        return fuel != null ? fuel.burnTime() : 0;
    }

    public static boolean isBurnable(ItemStack stack) {
        return getBurnTime(stack) > 0;
    }


    private static final DecimalFormat formatter;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        formatter = new DecimalFormat("#,###,###,###,###,###", symbols);
    }

    public static String format(int number) {
        return formatter.format(number);
    }

}
