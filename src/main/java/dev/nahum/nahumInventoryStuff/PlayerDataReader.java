package dev.nahum.nahumInventoryStuff;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

public class PlayerDataReader {
    public final ItemStackUtils itemUtils = new ItemStackUtils();
    private final NbtAccounter ACCOUNTER = NbtAccounter.create(32768 * 1024); // max of 32 mbs for user data

    public NbtAccounter getAccounter() {
        return ACCOUNTER;
    }

    public CompoundTag getPlayerData(UUID uuid) {
        File file = PathManager.getPlayerFile(uuid);
        try {
            return NbtIo.readCompressed(file.toPath(), ACCOUNTER);
        } catch (IOException e) {
            GoodLogger.error("Failed to read player data!\nError: " + e);
            return null;
        }
    }

    public CompoundTag getPlayerSnapshotData(File file) {
        try {
            return NbtIo.readCompressed(file.toPath(), ACCOUNTER);
        } catch (IOException e) {
            GoodLogger.error("Failed to read player data!\nError: " + e);
            return null;
        }
    }

    public Map<UUID, LinkedList<ListTag>> fetchAllOnlineUserData() {
        Map<UUID, LinkedList<ListTag>> returning = new HashMap<>();
        GoodLogger.info("Fetching all user data...");
        int count = 0;
        BackupManager backupManager = new BackupManager();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            try {
                LinkedList<ListTag> data = new LinkedList<>();

                ItemStack[] echest = onlinePlayer.getEnderChest().getContents();

                ItemStack[] mainInv = onlinePlayer.getInventory().getStorageContents();
                ItemStack[] armor = onlinePlayer.getInventory().getArmorContents();
                ItemStack[] offHand = onlinePlayer.getInventory().getExtraContents();

                ItemStack[] fullInventory = new ItemStack[Serializer.INVENTORYSIZE];

                System.arraycopy(mainInv, 0, fullInventory, 0, mainInv.length);
                System.arraycopy(armor, 0, fullInventory, mainInv.length, armor.length);
                System.arraycopy(offHand, 0, fullInventory, mainInv.length + armor.length, offHand.length);

                ListTag echestListTag = Serializer.serializeToListTag(echest, onlinePlayer);
                ListTag invListTag = Serializer.serializeToListTag(fullInventory, onlinePlayer);

                data.add(echestListTag);
                data.add(invListTag);
                returning.put(onlinePlayer.getUniqueId(), data);

                GoodLogger.info("Fetched online player: " + onlinePlayer.getName());
                count++;
            } catch (Exception e) {
                GoodLogger.error("Failed to fetch data for online player " + onlinePlayer.getName() + ": " + e.getMessage());
            }
        }

        GoodLogger.success("Fetched all data for " + count + " online users ;D");
        return returning;
    }

    public Map<UUID, LinkedList<ListTag>> fetchAllOfflineUserData(Map<UUID, LinkedList<ListTag>> onlineUserData) {
        Map<UUID, LinkedList<ListTag>> returning = new HashMap<>();
        GoodLogger.info("Fetching offline user data...");
        int count = 0;

        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            if (player.isOnline()) {
                continue;
            }

            UUID uuid = player.getUniqueId();
            CompoundTag tag = FileManager.getPlayerData(uuid);

            if (tag == null) {
                continue;
            }

            try {
                ListTag echestListTag = tag.getListOrEmpty(NbtTags.getEchest()).copy();
                ItemStack[] fullInventory = Serializer.buildFullInventoryFromPlayerTag(tag);
                boolean hasInventory = !echestListTag.isEmpty() || itemUtils.hasAnyItem(fullInventory);

                if (!hasInventory) {
                    continue;
                }

                ListTag invListTag = Serializer.serializeToListTag(fullInventory);

                LinkedList<ListTag> data = new LinkedList<>();
                data.add(echestListTag);
                data.add(invListTag);
                returning.put(uuid, data);

                count++;
            } catch (Exception e) {
                GoodLogger.error("Failed parsing offline data for UUID " + uuid + ": " + e.getMessage());
            }
        }

        GoodLogger.success("Fetched all data for " + count + " offline users ;D");
        returning.putAll(onlineUserData);
        return returning;
    }

    public Map<UUID, ListTag> fetchOneUserData(String type) {
        Map<UUID, ListTag> returning = new HashMap<>();
        if (!type.equals(NbtTags.getInventory()) && !type.equals(NbtTags.getEchest())) {
            GoodLogger.error("Fetched user data for incorrect type: " + type);
            return returning;
        }
        int count = 0;
        GoodLogger.info("Fetching " + type + " user data...");
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            ListTag listTag;
            CompoundTag tag = FileManager.getPlayerData(player.getUniqueId());
            if (player.isOnline()) {
                ItemStack[] contents;
                if (type.equals(NbtTags.getEchest())) {
                    contents = player.getPlayer().getEnderChest().getContents();
                } else if (type.equals(NbtTags.getInventory())) {
                    contents = player.getPlayer().getInventory().getContents();
                } else {
                    GoodLogger.warn("Failed to find player data for " + player.getName() + " due to datatype issues.");
                    continue;
                }

                if (contents == null) {
                    GoodLogger.warn("Failed to find player data for " + player.getName() + "!");
                    continue;
                }

                listTag = Serializer.serializeToListTag(contents, player);
                returning.put(player.getUniqueId(), listTag);
                GoodLogger.info("Fetched " + player.getName());
                count++;
                continue;
            }

            if (tag == null) {
                GoodLogger.warn(player.getName() + " has no player data!");
                continue;
            }

            listTag = tag.getListOrEmpty(type);
            if (listTag.isEmpty()) {
                GoodLogger.warn(player.getName() + " has no " + type + " data!");
                continue;
            }
            returning.put(player.getUniqueId(), listTag);
            GoodLogger.info("Fetched " + player.getName());
            count++;
        }
        GoodLogger.info("Fetched " + type + " data for " + count + " users ;D");
        return returning;
    }
}
