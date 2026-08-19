package dev.nahum.nahumInventoryStuff;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import org.bukkit.Bukkit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

public class FileManager {
    static public File createFolder(String parent, String subfolder) {
        File f = new File(parent, subfolder);
        if (!f.exists()) {
            if (f.mkdirs()) {
                return f;
            } else {
                GoodLogger.warn("Failed to create " + subfolder + " folder!");
                return null;
            }
        } else {
            return f;
        }
    }

    private static boolean deleteFolder(File directory) {
        File[] contents = directory.listFiles();
        if (contents != null) {
            for (File file : contents) {
                deleteFolder(file);
            }
        }
        return directory.delete();
    }

    static public boolean isFolderEmpty(File f) {
        try (Stream<Path> stream = Files.list(f.toPath())) {
            return stream.findAny().isEmpty();
        } catch (IOException e) {
            GoodLogger.warn("Failed to list player data!\nError: " + e);
            return true;
        }
    }

    //Backup system






    public static void loadAdminSnapshot(Object admin, Object player) {
        loadAdminSnapshot(getPlayerBuffer(admin, player), player);
    }

    public static void loadAdminSnapshot(File f, Object player) {
        GoodLogger.debug("started loadAdminSnapshot");
        UUID playerUuid = DataParser.getUuidFromObject(player);
        File snapshot = null;
        if (playerUuid == null) {
            GoodLogger.debug("Player is null!");
            return;
        }
        GoodLogger.debug("Player UUID: " + playerUuid);
        GoodLogger.debug("folder/file: " + f.getAbsolutePath());
        if (f.isDirectory()) {
            try (var stream = Files.list(f.toPath())) {
                Optional<Path> newestFile = stream
                        .filter(Files::isRegularFile)
                        .max(Comparator.comparingLong(p -> p.toFile().lastModified()));

                if (newestFile.isPresent()) {
                    snapshot = newestFile.get().toFile();
                } else {
                    GoodLogger.debug("No file found!");
                    return;
                }
            } catch (IOException e) {
                GoodLogger.debug("Error found: " + e.getMessage());
                e.printStackTrace();
            }
            GoodLogger.debug("got newest file");
        } else {
            snapshot = f;
        }
        if (snapshot != null) {
            GoodLogger.debug("snapshot wasn't null");
            CompoundTag snapshotTag = getPlayerSnapshotData(snapshot);
            ListTag inv = snapshotTag.getListOrEmpty(NbtTags.getInventory());
            ListTag echest = snapshotTag.getListOrEmpty(NbtTags.getEchest());
            CompoundTag additionalInfo = snapshotTag.getCompoundOrEmpty("additionalInfo");
            OfflinePlayer offlinePlayer = ((OfflinePlayer) player);
            if (offlinePlayer == null) {
                return;
            }
            GoodLogger.debug("OfflinePlayer " + offlinePlayer.getName());
            Player online = null;
            if (offlinePlayer.isOnline()) {
                online = offlinePlayer.getPlayer();
                GoodLogger.debug("Player was online");
            }
            GoodLogger.debug("player wasnt online");
            if (!inv.isEmpty() && online != null)
                pasteInventory(Serializer.buildFullInventoryFromPlayerTag(snapshotTag), online);
            if (!echest.isEmpty() && online != null)
                pasteEchest(Serializer.deserializeFromListTag(echest, Serializer.ECHESTSIZE), online);
            if (!inv.isEmpty())
                saveInventory(Serializer.buildFullInventoryFromPlayerTag(snapshotTag), offlinePlayer);
            if (!echest.isEmpty())
                saveEchest(Serializer.deserializeFromListTag(echest, Serializer.ECHESTSIZE), offlinePlayer.getUniqueId());
            if (!additionalInfo.isEmpty()) {
                String linkedTo = additionalInfo.getStringOr("linkedTo", "null");
                if (linkedTo.equals("null")) {
                    GoodLogger.debug("linkedTo is null");
                    return;
                }
                Path path = Paths.get(linkedTo);
                if (!path.toFile().exists()) {
                    return;
                }
                CompoundTag linkedToTag = getPlayerSnapshotData(path.toFile());
                GoodLogger.debug("linkedToPath: " + path);
                GoodLogger.debug("linkedToTag: " + linkedToTag);
                ListTag inv2 = linkedToTag.getListOrEmpty(NbtTags.getInventory());
                ListTag echest2 = linkedToTag.getListOrEmpty(NbtTags.getEchest());
                String uuidString = additionalInfo.getStringOr("uuid", "null");
                if (uuidString.equals("null")) {
                    return;
                }
                GoodLogger.debug("uuid: " + uuidString);
                offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuidString));
                online = null;
                if (offlinePlayer.isOnline()) {
                    online = offlinePlayer.getPlayer();
                    GoodLogger.debug("Player: "  + online.getName());
                }
                GoodLogger.debug("Player: "  + offlinePlayer.getName());
                if (!inv2.isEmpty() && online != null)
                    pasteInventory(Serializer.buildFullInventoryFromPlayerTag(linkedToTag), online);
                if (!echest2.isEmpty() && online != null)
                    pasteEchest(Serializer.deserializeFromListTag(echest2, Serializer.ECHESTSIZE), online);
                if (!inv2.isEmpty())
                    saveInventory(Serializer.buildFullInventoryFromPlayerTag(linkedToTag), offlinePlayer);
                if (!echest2.isEmpty())
                    saveEchest(Serializer.deserializeFromListTag(echest2, Serializer.ECHESTSIZE), offlinePlayer.getUniqueId());
                GoodLogger.debug("succeeded to restore both of their inventories");
            }
        }
    }

    public static void saveEchest(ItemStack[] contents, UUID uuid) {
        File file = getPlayerFile(uuid);
        CompoundTag tag = getPlayerData(uuid);

        tag.put(NbtTags.getEchest(), Serializer.serializeToListTag(contents));
        try {
            NbtIo.writeCompressed(tag, file.toPath());
        } catch (IOException e) {
            GoodLogger.error("Failed to save player data for " + Bukkit.getOfflinePlayer(uuid).getName() + ": \n" + e.getMessage());
        }
    }

    public static void saveInventory(ItemStack[] contents, Object recipient) {
        UUID uuid = DataParser.getUuidFromObject(recipient);
        File file = getPlayerFile(uuid);
        CompoundTag tag = getPlayerData(uuid);

        if (tag == null) {
            GoodLogger.warn("Failed to save player data for " + Bukkit.getOfflinePlayer(uuid).getName() + "!");
            return;
        }

        if (contents != null) {
            tag.put(NbtTags.getInventory(), Serializer.serializeToListTag(
                    DataParser.getItemStackArray(contents, Serializer.MAIN_INVENTORY_SIZE, 0)));

            tag.put(NbtTags.getEquipment(), Serializer.serializeArmorToCompoundTag(
                    DataParser.getItemStackArray(contents, Serializer.ARMORSIZE + 1, Serializer.ARMOR_START)));
        }

        try {
            NbtIo.writeCompressed(tag, file.toPath());
        } catch (IOException e) {
            GoodLogger.error("Failed to save player data for " + Bukkit.getOfflinePlayer(uuid).getName() + ": \n" + e.getMessage());
        }
    }

    public static void saveSnapshot(ItemStack[] inventory, ItemStack[] echest, UUID uuid, File file) {
        CompoundTag tag = new CompoundTag();

        if (inventory != null) {
            tag.put(NbtTags.getInventory(), Serializer.serializeToListTag(
                    DataParser.getItemStackArray(inventory, Serializer.MAIN_INVENTORY_SIZE, 0)));

            tag.put(NbtTags.getEquipment(), Serializer.serializeToListTag(
                    DataParser.getItemStackArray(inventory, Serializer.ARMORSIZE, Serializer.ARMOR_START)));

            tag.put(NbtTags.getOffhand(), Serializer.serializeToListTag(
                    DataParser.getItemStackArray(inventory, 1, Serializer.OFFHAND_SLOT)));
        }

        if (echest != null) {
            tag.put(NbtTags.getEchest(), Serializer.serializeToListTag(echest));
        }

        try {
            NbtIo.writeCompressed(tag, file.toPath());
        } catch (IOException e) {
            GoodLogger.error("Failed to save player data for " + Bukkit.getOfflinePlayer(uuid).getName() + ": \n" + e.getMessage());
        }
    }

    public static boolean writeString(File snapshot, UUID uuid, String message, String key) {
        CompoundTag tag = getPlayerSnapshotData(snapshot);
        CompoundTag stringTag = tag.getCompoundOrEmpty("additionalInfo");
        if (stringTag.isEmpty()) {
            stringTag = new CompoundTag();
        }
        stringTag.putString(key, message);

        tag.put("additionalInfo", stringTag);

        try {
            NbtIo.writeCompressed(tag, snapshot.toPath());
            return true;
        } catch (IOException e) {
            GoodLogger.error("Failed to save player data for " + uuid.toString() + ": \n" + e.getMessage());
            return false;
        }
    }

    public static void pasteInventory(ItemStack[] contents, Player recipient) {
        recipient.getInventory().setContents(
                DataParser.getItemStackArray(contents, Serializer.MAIN_INVENTORY_SIZE, 0));

        recipient.getInventory().setArmorContents(
                DataParser.getItemStackArray(contents, Serializer.ARMORSIZE, Serializer.ARMOR_START));

        recipient.getInventory().setItemInOffHand(
                DataParser.getItemStackArray(contents, 1, Serializer.OFFHAND_SLOT)[0]);
        recipient.updateInventory();
    }

    public static void pasteEchest(ItemStack[] contents, Player recipient) {
        recipient.getInventory().setContents(contents);
        recipient.updateInventory();
    }

    public static ItemStack[] loadInventory(Object giver) {
        return Serializer.buildFullInventoryFromPlayerTag(getPlayerData(DataParser.getUuidFromObject(giver)));
    }

    public static String cleanName(String name) {
        if (name.endsWith(".nahumbackup")) {
            name = name.replace(".nahumbackup", "");
        }
        name = name.replaceAll("-\\(\\d+\\)$", "");
        return name;
    }

    public static LocalDate getAgeFromName(String fileName) {
        fileName = cleanName(fileName);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        LocalDateTime dateTime = LocalDateTime.parse(fileName, formatter);
        return dateTime.toLocalDate();
    }

    public static LocalDateTime getDateTimeFromName(String fileName) {
        fileName = cleanName(fileName);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        LocalDateTime dateTime = LocalDateTime.parse(fileName, formatter);
        return dateTime;
    }

    public static void deleteOldSnapshots(File folder, String name) {
        int max;
        try {
            max = Integer.parseInt((String) ConfigManager.getConfig(name));
        } catch (Exception exception) {
            GoodLogger.warn("Failed to delete old snapshots for " + name + ": \n" + exception.getMessage());
            return;
        }

        List<File> files = new ArrayList<>();
        try (Stream<Path> stream = Files.list(folder.toPath())) {
            stream.filter(Files::isRegularFile)
                    .forEach(path -> files.add(path.toFile()));
        } catch (IOException e) {
            System.err.println("Error reading directory: " + e.getMessage());
            return;
        }

        if (max == 0 || files.size() <= max) return; // Nothing to delete

        Map<LocalDateTime, File> map = new HashMap<>();
        List<LocalDateTime> dates = new ArrayList<>();

        for (File file : files) {
            LocalDateTime date = getDateTimeFromName(file.getName());
            if (date != null) {
                map.put(date, file);
                dates.add(date);
            }
        }

        dates.sort(Comparator.naturalOrder());
        int toDelete = files.size() - max;
        int deleted = 0;

        for (LocalDateTime date : dates) {
            if (deleted >= toDelete) break;

            File file = map.get(date);
            if (file != null && file.delete()) {
                GoodLogger.debug("Deleted old snapshot: " + file.getAbsolutePath());
                deleted++;
            } else if (file != null) {
                GoodLogger.debug("Failed to delete: " + file.getAbsolutePath());
            }
        }
        GoodLogger.debug("finished deleting snapshots with max of " + max + " and size of " + files.size() + " deleted " + deleted);

    }

    public static void deleteOldPlayers(File folder, String name) {
        int max;
        try {
            max = Integer.parseInt((String) ConfigManager.getConfig(name));
        } catch (Exception exception) {
            GoodLogger.warn("Failed to delete old snapshots for " + name + ": \n" + exception.getMessage());
            return;
        }

        if (max == 0) return; // Keep all

        List<File> playerDirs = new ArrayList<>();
        Map<FileTime, File> timeToFile = new HashMap<>();
        List<FileTime> creationTimes = new ArrayList<>();

        // Read all player directories and their creation times
        try (Stream<Path> stream = Files.list(folder.toPath())) {
            stream.filter(Files::isDirectory)
                    .forEach(path -> {
                        try {
                            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
                            FileTime creationTime = attr.creationTime();
                            File file = path.toFile();

                            playerDirs.add(file);
                            timeToFile.put(creationTime, file);
                            creationTimes.add(creationTime);
                        } catch (IOException e) {
                            GoodLogger.warn("Could not read attributes for: " + path.getFileName());
                        }
                    });
        } catch (IOException e) {
            GoodLogger.warn("Error reading directory: " + e.getMessage());
            return;
        }
        if (playerDirs.size() <= max) return;
        creationTimes.sort(FileTime::compareTo);
        int toDelete = playerDirs.size() - max;
        int deleted = 0;

        for (FileTime time : creationTimes) {
            if (deleted >= toDelete) break;

            File playerDir = timeToFile.get(time);
            if (playerDir == null) continue;

            // Delete directory recursively
            if (deleteFolder(playerDir)) {
                GoodLogger.debug("Deleted old player data: " + playerDir.getAbsolutePath());
                deleted++;
            } else {
                GoodLogger.warn("Failed to delete: " + playerDir.getAbsolutePath());
            }
        }
    }

    public static boolean writeTimeAndMessage(File snapshot, UUID uuid, String message, String key) {
        CompoundTag tag = getPlayerSnapshotData(snapshot);
        ListTag listTag = new ListTag();
        listTag.add(Serializer.serializeString(message, key));
        listTag.add(Serializer.serializeString(Instant.now().toString(), "time"));

        try {
            NbtIo.writeCompressed(tag, snapshot.toPath());
            return true;
        } catch (IOException e) {
            GoodLogger.error("Failed to save player data for " + uuid.toString() + ": \n" + e.getMessage());
            try {
                Files.delete(snapshot.toPath());
            } catch (IOException e1) {
                GoodLogger.error("Failed to delete failed snapshot file: " + snapshot.getName() + "\n Because: " + e.getMessage());
            }
            return false;
        }
    }

    public static File makeNewSnapshot(File folder, String name) {
        File newSnapshot;
        if (name == null) {
            LocalDateTime now = LocalDateTime.now();
            newSnapshot = new File(folder.toPath() +
                    File.separator + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".nahumbackup");

            for (int i = 1; newSnapshot.exists(); i++) {
                newSnapshot = new File(folder.toPath() +
                        File.separator + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) +
                        "-(" + i + ")" + ".nahumbackup");
            }
            NahumInventoryStuff.deleteOldBackups(folder);
        } else {
            newSnapshot = new File(folder.toPath().toString(), name + ".nahumbackup");
        }
        return newSnapshot;
    }

    public static boolean performPlayerSaveSnapshot(Object player, ItemStack[] inventory, ItemStack[] echest, String message, String name) {
        UUID uuid = DataParser.getUuidFromObject(player);
        File folder = PathManager.getPlayerDeathsFolder(uuid);

        if (uuid == null) {
            GoodLogger.warn("Couldn't get last dead player's uuid");
            return false;
        }

        File snapshot = makeNewSnapshot(folder, null);
        saveSnapshot(inventory, echest, uuid, snapshot);
        deleteOldSnapshots(folder, name);

        if (!writeTimeAndMessage(snapshot, uuid, message, "message")) {
            GoodLogger.warn("Couldn't save player's snapshot");
            return false;
        }

        name = name.toLowerCase().replace("max", "");
        name = name.replace("snapshot", "");

        GoodLogger.debug(((OfflinePlayer) player).getName() + "'s " + name + " snapshot saved to: " + snapshot.getAbsolutePath());
        return true;
    }

    public static String performAdminBufferSave(
            Object admin, Object player, ItemStack[] inventory, ItemStack[] echest, String message, String name, String otherPath
    ) {
        if (name == null) {
            name = "maxSnapshots";
        }
        UUID playerUuid = DataParser.getUuidFromObject(player);
        File folder = getPlayerBuffer(admin, player);

        if (playerUuid == null) {
            GoodLogger.warn("Couldn't get last player's uuid");
            return null;
        }

        File snapshot = makeNewSnapshot(folder, null);
        saveSnapshot(inventory, echest, playerUuid, snapshot);
        deleteOldSnapshots(folder, name);
        deleteOldPlayers(folder.getParentFile(), name);

        if (!writeTimeAndMessage(snapshot, playerUuid, message, "message")) {
            GoodLogger.warn("Couldn't save player's snapshot");
            return null;
        }

        writeString(snapshot, playerUuid, playerUuid.toString(), "uuid");

        if (otherPath != null) {
            writeString(snapshot, playerUuid, otherPath, "linkedTo");
        }

        GoodLogger.debug(((OfflinePlayer) admin).getName() + "'s actions on " + ((OfflinePlayer) player).getName() + "'s inventories saved to: " + snapshot.getAbsolutePath());
        return snapshot.getAbsolutePath();
    }

    public static boolean updateSnapshot(Path path, Object admin, Object player, String toWrite, String key) {
        File snapshot = path.toFile();
        UUID playerUuid = DataParser.getUuidFromObject(player);
        return writeString(snapshot, playerUuid, toWrite, key);
    }
}