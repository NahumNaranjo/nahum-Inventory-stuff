package dev.nahum.nahumInventoryStuff;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
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
        switch (object) {
            case Player player -> uuid = player.getUniqueId();
            case OfflinePlayer offlinePlayer -> uuid = offlinePlayer.getUniqueId();
            case UUID uuid1 -> {
                return uuid1;
            }
            case String s -> uuid = UUID.fromString(s);
            case null, default -> {
                return null;
            }
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


    public static ItemStack[] objectToItemStackArr(Object obj) {
        return switch (obj) {
            case ListTag tags -> Serializer.deserializeFromListTag(tags, Serializer.MAIN_INVENTORY_SIZE);
            case CompoundTag compoundTag -> Serializer.buildFullInventoryFromPlayerTag(compoundTag);
            case Inventory inventory -> inventory.getContents();
            case ItemStack[] itemStacks -> itemStacks;
            case null, default -> new ItemStack[0];
        };
    }

}
