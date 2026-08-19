package dev.nahum.nahumInventoryStuff;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BackupManager {
    public final File folder = PathManager.getBackupFolder();
    public PlayerDataReader reader;
    public PlayerDataWriter writer;

    BackupManager(PlayerDataReader reader, PlayerDataWriter writer) {
        this.reader = Objects.requireNonNull(reader, "reader cannot be null");
        this.writer = Objects.requireNonNull(writer, "writer cannot be null");
    }

    private File getLastBackup() {
        if (FileManager.isFolderEmpty(folder)) {
            GoodLogger.warn("Failed to list player data in backup folder!");
            return null;
        }

        try (var stream = Files.list(folder.toPath())) {
            Optional<Path> latestFile = stream
                    .filter(Files::isRegularFile) // Exclude directories
                    .max(Comparator.comparingLong(p -> p.toFile().lastModified()));

            if (latestFile.isPresent()) {
                return latestFile.get().toFile();
            } else {
                GoodLogger.warn("No files found in the directory.");
            }
        } catch (IOException e) {
            GoodLogger.warn("Failed to list player data in backup folder!\nError: " + e);
            e.printStackTrace();
        }
        return null;
    }

    public boolean writeBackup(String name, Map<UUID, LinkedList<ListTag>> onlineUsers) {
        if (folder == null) {
            GoodLogger.error("Failed to find backup folder!");
            return false;
        }
        File newBackup;
        if (name == null) {
            LocalDateTime now = LocalDateTime.now();
            newBackup = new File(folder.toPath() +
                    File.separator + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".nahumbackup");

            for (int i = 1; newBackup.exists(); i++) {
                newBackup = new File(folder.toPath() +
                        File.separator + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) +
                        "-(" + i + ")" + ".nahumbackup");
            }
        } else {
            newBackup = new File(folder.toPath().toString(), name + ".nahumbackup");
        }

        Map<UUID, LinkedList<ListTag>> toWrite = PlayerDataReader.fetchAllOfflineUserData(onlineUsers);

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
        if (folder == null) {
            GoodLogger.error("Failed to find required folders!");
            return false;
        }

        File backupFile = (toRead == null) ? getLastBackup() : new File(folder, toRead);
        if (backupFile == null || !backupFile.exists()) {
            GoodLogger.error("Failed to find backup file!");
            return false;
        }

        try {
            CompoundTag rootTag = NbtIo.readCompressed(backupFile.toPath(), ACCOUNTER);

            for (String uuidString : rootTag.keySet()) {
                UUID uuid = UUID.fromString(uuidString);

                ListTag playerDataList = rootTag.getListOrEmpty(uuidString);

                if (playerDataList.size() >= 2) {
                    ListTag echestData = playerDataList.getListOrEmpty(0);
                    ListTag invData = playerDataList.getListOrEmpty(1);

                    OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

                    if (player.getPlayer() != null && player.getPlayer().isOnline()) {
                        var onlinePlayer = player.getPlayer();

                        onlinePlayer.getEnderChest().setContents(
                                Serializer.deserializeFromListTag(echestData, Serializer.ECHESTSIZE)
                        );

                        ItemStack[] fullInventory = Serializer.deserializeFromListTag(invData, Serializer.INVENTORYSIZE);
                        ItemStack[] mainInventory = new ItemStack[Serializer.MAIN_INVENTORY_SIZE];
                        System.arraycopy(fullInventory, 0, mainInventory, 0, Serializer.MAIN_INVENTORY_SIZE);

                        onlinePlayer.getInventory().setStorageContents(mainInventory);
                        onlinePlayer.getInventory().setArmorContents(new ItemStack[]{
                                fullInventory[Serializer.ARMOR_START],
                                fullInventory[Serializer.ARMOR_START + 1],
                                fullInventory[Serializer.ARMOR_START + 2],
                                fullInventory[Serializer.ARMOR_START + 3]
                        });
                        onlinePlayer.getInventory().setItemInOffHand(fullInventory[Serializer.OFFHAND_SLOT]);
                        continue;
                    }

                    File playerFile = getPlayerFile(uuid);
                    CompoundTag existingPlayerTag;

                    if (playerFile.exists()) {
                        existingPlayerTag = NbtIo.readCompressed(playerFile.toPath(), ACCOUNTER);
                    } else {
                        existingPlayerTag = new CompoundTag();
                    }

                    ItemStack[] fullInventory = Serializer.deserializeFromListTag(invData, Serializer.INVENTORYSIZE);
                    existingPlayerTag.put(NbtTags.getEchest(), echestData);
                    Serializer.applyFullInventoryToPlayer(existingPlayerTag, fullInventory);

                    NbtIo.writeCompressed(existingPlayerTag, playerFile.toPath());
                    GoodLogger.info("Restored data for " + uuid);
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
