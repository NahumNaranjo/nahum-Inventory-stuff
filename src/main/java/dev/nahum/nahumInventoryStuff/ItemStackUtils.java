package dev.nahum.nahumInventoryStuff;

import org.bukkit.inventory.ItemStack;

public class ItemStackUtils {
    public boolean hasAnyItem(ItemStack[] items) {
        for (ItemStack item : items) {
            if (item != null && !item.getType().isAir()) {
                return true;
            }
        }
        return false;
    }
}
