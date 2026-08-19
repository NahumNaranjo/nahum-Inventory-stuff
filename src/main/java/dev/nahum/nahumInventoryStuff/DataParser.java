package dev.nahum.nahumInventoryStuff;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.util.List;
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
        } else if (object instanceof String) {
            uuid = UUID.fromString((String) object);
        } else {
            return null;
        }
        return uuid;
    }

    public static FileTime getOldestFileTime(List<FileTime> list) {
        FileTime oldest = null;
        for(FileTime fileTime : list) {
            if(oldest == null || oldest.toInstant().isAfter(fileTime.toInstant())) {
                oldest = fileTime;
            }
        }
        return oldest;
    }

    public static LocalDateTime getOldestLocalDateTime(List<LocalDateTime> list) {
        LocalDateTime oldest = null;
        for(LocalDateTime localDateTime : list) {
            if(oldest == null || oldest.isAfter(localDateTime)) {
                oldest = localDateTime;
            }
        }
        return oldest;
    }

}
