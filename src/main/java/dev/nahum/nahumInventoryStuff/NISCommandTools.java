package dev.nahum.nahumInventoryStuff;

import net.minecraft.nbt.ListTag;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

public class NISCommandTools {
    private PlayerDataReader reader = new PlayerDataReader();
    private PlayerDataWriter writer = new PlayerDataWriter(reader);
    private BackupManager backup = new BackupManager(reader, writer);
    private SnapshotManager snapshot = new SnapshotManager(reader, writer);
    private CommandSender sender;
    private SenderLogger toSender;
    public NISCommandTools(CommandSender sender) {
        this.sender = sender;
        this.toSender = new SenderLogger(sender);
        this.reader = new PlayerDataReader();
        this.writer = new PlayerDataWriter(reader);
        this.backup = new BackupManager(reader, writer);
        this.snapshot = new SnapshotManager(reader, writer);
    }

    private boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }

    public void restore(String name) {
        if (!hasPermission("nahum.nahumstuff") && !hasPermission("nahum.nahumstuff.restore")) {
            toSender.error("You do not have permission to use this command.");
            return;
        }
        GoodLogger.debug(sender.getName() + " is trying to restore from a backup.");

        if (name == null) {
            if (!backup.readBackup(null)) {
                toSender.error("Couldn't find backups.");
            } else {
                toSender.success("Correctly restored backup.");
            }
            return;
        }

        name = FileNameManager.checkCustomBackupName(name);

        if (name == null) {
            toSender.error("Couldn't find a valid backup with that name.");
            return;
        }

        if (!backup.readBackup(name)) {
            toSender.error("Something went wrong. Check console");
        } else {
            toSender.success("Correctly restored backup.");
        }
    }

    public void backup(String name) {
        if (!hasPermission("nahum.nahumstuff") && !hasPermission("nahum.nahumstuff.backup")) {
            toSender.error("You do not have permission to use this command.");
            return;
        }
        GoodLogger.info(sender.getName() + " is trying to save a backup.");

        Map<UUID, LinkedList<ListTag>> onlineUsers = reader.fetchAllOnlineUserData();
        boolean result;
        result = backup.writeBackup(name, onlineUsers);
        if (result) {
            toSender.success("Successfully saved backup.");
        } else {
            toSender.success("Something went wrong. Check console");
        }
    }

    public void debug(String arg) {
        if (!hasPermission("nahum.nahumstuff") && !hasPermission("nahum.nahumstuff.debug")) {
            toSender.error("You do not have permission to use this command.");
            return;
        }
        if (arg == null) {
            ConfigManager.setOnDebug(!ConfigManager.getConfigOrDefault("debug", true));
            if (ConfigManager.getConfigOrDefault("debug", false)) {
                toSender.success("Debug mode enabled.");
                GoodLogger.debug("Debug mode enabled by " + sender.getName());
            } else {
                toSender.success("Debug mode disabled.");
                GoodLogger.info("Debug mode disabled by " + sender.getName());
            }
            return;
        }
        arg = arg.toLowerCase();
        if (arg.equals("on") || arg.equals("1") || arg.equals("true") || arg.equals("start")) {
            if (ConfigManager.getConfigOrDefault("debug", false)) {
                toSender.success("Debug mode was already enabled.");
                return;
            }

            ConfigManager.setOnDebug(!ConfigManager.getConfigOrDefault("debug", true));
            toSender.success("Debug mode enabled.");
            GoodLogger.info("Debug mode enabled by " + sender.getName());
        }
        if (arg.equals("off") || arg.equals("0") || arg.equals("false") || arg.equals("stop")) {
            if (ConfigManager.getConfigOrDefault("debug", false) == false) {
                toSender.success("Debug mode was already disabled.");
                return;
            }

            ConfigManager.setConfig("onDebug", false);
            toSender.success("Debug mode disabled.");
            GoodLogger.info("Debug mode disabled by " + sender.getName());
        }
    }

    public void undo(String name){
        if (name == null) {
            toSender.warning("Usage: /nis undo <player>");
            return;
        }
        Object admin;
        if (sender instanceof Player) {
            admin = (Player)sender;
        } else {
            admin = "console";
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(name);
        if (target == null) {
            toSender.error("That player is not valid.");
            return;
        }
        writer.loadAdminSnapshot(admin, target);
    }

    public ConfigDatum config(String set, String name, String value){
        ConfigDatum datum = ConfigManager.getConfigDatum(name);

        if(datum == null){
            toSender.error("That's not a valid config name.");
            GoodLogger.debug("Tried to fetch " + name + " config but failed.");
            return null;
        }

        if(set.equalsIgnoreCase("set")){
            if(datum.isCompatible(value)){
                ConfigManager.setConfig(name, value);
                datum.setValue(value);
            }
        }
        return datum;
    }
}
