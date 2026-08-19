package dev.nahum.nahumInventoryStuff;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class DataParser {
    public static ItemStack[] getItemStackArray(ItemStack[] items, int size, int slot) {
        ItemStack[] stack = new ItemStack[size];
        System.arraycopy(items, slot, stack, 0, size);
        return stack;
    }

    public static UUID getUuidFromObject(Object object) {
        UUID uuid;
        if (object instanceof Player player) {
            uuid = player.getUniqueId();
        } else if (object instanceof OfflinePlayer offlinePlayer) {
            uuid = offlinePlayer.getUniqueId();
        } else if (object instanceof UUID) {
            return (UUID) object;
        } else {
            return null;
        }
        return uuid;
    }

}
