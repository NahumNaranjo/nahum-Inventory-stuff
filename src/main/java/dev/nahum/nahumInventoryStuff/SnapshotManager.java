package dev.nahum.nahumInventoryStuff;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

public class SnapshotManager {
    private PlayerDataReader reader;
    private PlayerDataWriter writer;

    SnapshotManager(PlayerDataReader reader, PlayerDataWriter writer) {
        this.reader = Objects.requireNonNull(reader, this.getClass().getName() + ": reader cannot be null.");
        this.writer = Objects.requireNonNull(writer, this.getClass().getName() + ": writer cannot be null.");
    }



    public boolean writeTimeAndMessage(File snapshot, UUID uuid, String message, String key) {
        CompoundTag tag = reader.getPlayerSnapshotData(snapshot);
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

    public File makeNewSnapshot(File folder, String name) {
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

    public boolean performPlayerSaveSnapshot(Object player, ItemStack[] inventory, ItemStack[] echest, String message, String name) {
        UUID uuid = DataParser.getUuidFromObject(player);
        File folder = PathManager.getPlayerDeathsFolder(uuid);

        if (uuid == null) {
            GoodLogger.warn("Couldn't get last dead player's uuid");
            return false;
        }

        File snapshot = makeNewSnapshot(folder, null);
        writer.saveSnapshot(inventory, echest, uuid, snapshot);
        CleanupManager.deleteOldSnapshots(folder, name);

        if (!writeTimeAndMessage(snapshot, uuid, message, "message")) {
            GoodLogger.warn("Couldn't save player's snapshot");
            return false;
        }

        name = name.toLowerCase().replace("max", "");
        name = name.replace("snapshot", "");

        GoodLogger.debug(((OfflinePlayer) player).getName() + "'s " + name + " snapshot saved to: " + snapshot.getAbsolutePath());
        return true;
    }

    public String performAdminBufferSave(
            Object admin, Object player, ItemStack[] inventory, ItemStack[] echest, String message, String name, String otherPath
    ) {
        if (name == null) {
            name = "maxSnapshots";
        }
        UUID playerUuid = DataParser.getUuidFromObject(player);
        File folder = PathManager.getPlayerBuffer(admin, player);

        if (playerUuid == null) {
            GoodLogger.warn("Couldn't get last player's uuid");
            return null;
        }

        File snapshot = makeNewSnapshot(folder, null);
        writer.saveSnapshot(inventory, echest, playerUuid, snapshot);
        CleanupManager.deleteOldSnapshots(folder, name);
        CleanupManager.deleteOldPlayers(folder.getParentFile(), name);

        if (!writeTimeAndMessage(snapshot, playerUuid, message, "message")) {
            GoodLogger.warn("Couldn't save player's snapshot");
            return null;
        }

        writer.writeStringToSnapshot(snapshot, playerUuid, playerUuid.toString(), "uuid");

        if (otherPath != null) {
            writer.writeStringToSnapshot(snapshot, playerUuid, otherPath, "linkedTo");
        }

        GoodLogger.debug(((OfflinePlayer) admin).getName() + "'s actions on " + ((OfflinePlayer) player).getName() + "'s inventories saved to: " + snapshot.getAbsolutePath());
        return snapshot.getAbsolutePath();
    }

    public boolean updateSnapshot(Path path, Object admin, Object player, String toWrite, String key) {
        File snapshot = path.toFile();
        UUID playerUuid = DataParser.getUuidFromObject(player);
        return writer.writeStringToSnapshot(snapshot, playerUuid, toWrite, key);
    }
}
