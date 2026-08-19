package dev.nahum.nahumInventoryStuff;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class BackupManager {
    public final File folder = PathManager.getBackupFolder();
    public PlayerDataReader reader;
    public PlayerDataWriter writer;

    BackupManager(PlayerDataReader reader, PlayerDataWriter writer) {
        this.reader = Objects.requireNonNull(reader, this.getClass().getName() + ": reader cannot be null");
        this.writer = Objects.requireNonNull(writer, this.getClass().getName() + ": writer cannot be null");
    }

    private File getLastBackup() {
        if (FileManager.isFolderEmpty(folder)) {
            GoodLogger.warn("Failed to list player data in backup folder!");
            return null;
        }

        return FileManager.getNewestFileFromAtt(folder.toPath().toString());
    }



    public boolean writeBackup(String name, Map<UUID, LinkedList<ListTag>> onlineUsers) {
        File newBackup;
        if (name == null) {
            newBackup = FileNameManager.getBackupName(folder);
        } else {
            newBackup = new File(folder.toPath().toString(), name + ".nahumbackup");
        }

        Map<UUID, LinkedList<ListTag>> toWrite = reader.fetchAllOfflineUserData(onlineUsers);

        CompoundTag rootTag = new CompoundTag();

        toWrite.forEach((uuid, listTag) -> {
            ListTag playerBackupGroup = new ListTag();
            for (ListTag playerListTag : listTag) {
                playerBackupGroup.add(playerListTag);
            }

            rootTag.put(uuid.toString(), playerBackupGroup);
        });

        try {
            NbtIo.writeCompressed(rootTag, newBackup.toPath());
            return true;
        } catch (IOException e) {
            GoodLogger.error("Failed to write backup file!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean readBackup(String toRead) {
        File backupFile = (toRead == null) ? getLastBackup() : new File(folder, toRead);
        if (backupFile == null || !backupFile.exists()) {
            GoodLogger.error("Failed to find backup file!");
            return false;
        }

        try {
            CompoundTag rootTag = NbtIo.readCompressed(backupFile.toPath(), reader.getAccounter());

            for (String uuidString : rootTag.keySet()) {
                UUID uuid = UUID.fromString(uuidString);

                ListTag playerDataList = rootTag.getListOrEmpty(uuidString);

                if (playerDataList.size() >= 2) {
                    ListTag echestData = playerDataList.getListOrEmpty(0);
                    ListTag invData = playerDataList.getListOrEmpty(1);

                    OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

                    if (player.getPlayer() != null && player.getPlayer().isOnline()) {
                        var onlinePlayer = player.getPlayer();

                        writer.pasteEchest(echestData, onlinePlayer);
                        writer.pasteEchest(invData, onlinePlayer);
                        continue;
                    }

                    File playerFile = PathManager.getPlayerFile(uuid);
                    CompoundTag existingPlayerTag;

                    if (playerFile.exists()) {
                        existingPlayerTag = NbtIo.readCompressed(playerFile.toPath(), reader.getAccounter());
                    } else {
                        existingPlayerTag = new CompoundTag();
                    }

                    ItemStack[] fullInventory = Serializer.deserializeFromListTag(invData, Serializer.INVENTORYSIZE);
                    existingPlayerTag.put(NbtTags.getEchest(), echestData);
                    Serializer.applyFullInventoryToPlayer(existingPlayerTag, fullInventory);

                    NbtIo.writeCompressed(existingPlayerTag, playerFile.toPath());
                    GoodLogger.debug("Restored data for " + uuid);
                    continue;
                }
                GoodLogger.warn("Failed to restore data due to invalid list format for " + uuid);
            }
            return true;
        } catch (IOException e) {
            GoodLogger.error("Failed to read backup file!\nError: " + e);
            return false;
        }
    }
}
