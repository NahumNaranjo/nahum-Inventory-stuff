package dev.nahum.nahumInventoryStuff;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.UUID;

public class PathManager {
    private PathManager() {} // no instantiation for you >:(

    static public File getWorldDir() {
        return Bukkit.getWorlds().getFirst().getWorldFolder();
    }

    static public File getPlayerFolder() {
        return new File(getWorldDir(), "playerdata");
    }

    static public File getDataFolder() {
        return NahumInventoryStuff.getInstance().getDataFolder();
    }

    static public File getPlayerFile(UUID uuid) {
        return new File(getPlayerFolder(), uuid.toString() + ".dat");
    }

    static public File getSnapshotFolder() {
        return FileManager.createFolder(getDataFolder().toString(), "snapshots");
    }

    static public File getBufferFolder() {
        return FileManager.createFolder(getSnapshotFolder().toString(), "buffers");
    }

    static public File getAdminFolder(Object player) {
        if (player == null || player instanceof String || player instanceof CommandSender) {
            return FileManager.createFolder(getBufferFolder().toString(), "console");
        } else {
            return FileManager.createFolder(getBufferFolder().toString(), DataParser.getUuidFromObject(player).toString());
        }

    }

    static public File getPlayerBuffer(Object admin, Object player) {
        return FileManager.createFolder(getAdminFolder(admin).toString(), DataParser.getUuidFromObject(player).toString());
    }

    static public File getPlayersSnapshotFolder() {
        return FileManager.createFolder(getSnapshotFolder().toString(), "players");
    }

    static public File getLonePlayerSnapshotFolder(Object player) {
        return FileManager.createFolder(getPlayersSnapshotFolder().toString(), DataParser.getUuidFromObject(player).toString());
    }

    static public File getPlayerDeathsFolder(Object player) {
        return FileManager.createFolder(getLonePlayerSnapshotFolder(player).toString(), "deaths");
    }

    static public File getPlayerJoinsFolder(Object player) {
        return FileManager.createFolder(getPlayersSnapshotFolder().toString(), "joins");
    }

    static public File getPlayerLeavesFolder(Object player) {
        return FileManager.createFolder(getPlayersSnapshotFolder().toString(), "leaves");
    }

    static public File getPlayerForcedFolder(Object player) {
        return FileManager.createFolder(getPlayersSnapshotFolder().toString(), "ForcedSaves");
    }

    static public File getBackupFolder() {
        File backupFolder = new File(getDataFolder(), "backup");
        if (!backupFolder.exists()) {
            try {
                backupFolder.mkdirs();
            } catch (SecurityException e) {
                GoodLogger.error("Failed to create backup folder!\nError: " + e);
            }
        }
        return backupFolder;
    }

}
