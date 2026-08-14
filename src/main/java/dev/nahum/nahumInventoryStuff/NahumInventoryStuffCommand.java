package dev.nahum.nahumInventoryStuff;

import net.minecraft.nbt.ListTag;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class NahumInventoryStuffCommand implements TabExecutor {
    @Override
    public boolean onCommand (CommandSender sender, Command cmd, String label, String[]args){
        if(args.length==0){
            sender.sendMessage(ChatColor.GREEN + NahumInventoryStuff.getCredits());
            return true;
        }

        switch(args[0].toLowerCase()){
            case "restore":
                if(!sender.hasPermission("nahum.nahumstuff") && !sender.hasPermission("nahum.nahumstuff.restore")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                if(args.length==2){
                    GoodLogger.info(sender.getName() + " is trying to restore from a backup.");
                    String name = getBackupFileName(args[1]);
                    if(name==null){
                        sender.sendMessage(ChatColor.RED + "Something went wrong. Check console");
                        return true;
                    }
                    if(!FileManager.readBackup(name)){
                        sender.sendMessage(ChatColor.RED + "Something went wrong. Check console");
                    } else {
                        sender.sendMessage(ChatColor.GREEN + "Correctly restored backup.");
                    }
                } else {
                    if(!FileManager.readBackup(null)){
                        sender.sendMessage(ChatColor.RED + "Something went wrong. Check console");
                    } else {
                        sender.sendMessage(ChatColor.GREEN + "Correctly restored backup.");
                    }
                }
                return true;
            case "backup":
                if(!sender.hasPermission("nahum.nahumstuff") && !sender.hasPermission("nahum.nahumstuff.backup")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                GoodLogger.info(sender.getName() + " is trying to save a backup.");
                Map<UUID, LinkedList<ListTag>> onlineUsers = FileManager.fetchAllOnlineUserData();
                List<UUID> uuids =  new LinkedList<>();
                    boolean result;
                    if(args.length==2){
                        result = FileManager.writeBackup(args[1], onlineUsers);
                    } else {
                        result = FileManager.writeBackup(null, onlineUsers);
                    }
                    if(result){
                         sender.sendMessage(ChatColor.GREEN + "Successfully saved backup.");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Something went wrong. Check console");
                    }
                break;
            case "debug":
                if(!sender.hasPermission("nahum.nahumstuff") && !sender.hasPermission("nahum.nahumstuff.debug")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                if(args.length==1){
                    NahumInventoryStuff.setOnDebug(!NahumInventoryStuff.getOnDebug());
                    if(NahumInventoryStuff.getOnDebug()){
                        sender.sendMessage(ChatColor.GREEN + "Debug mode enabled.");
                        GoodLogger.info("Debug mode enabled by " + sender.getName());
                    } else {
                        sender.sendMessage(ChatColor.GREEN + "Debug mode disabled.");
                        GoodLogger.info("Debug mode disabled by " + sender.getName());
                    }
                    return true;
                }
                if(
                        args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("1") ||
                        args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("start")
                ){
                    if(NahumInventoryStuff.getOnDebug()){
                        sender.sendMessage(ChatColor.GREEN + "Debug mode was already enabled.");
                        return true;
                    }

                    NahumInventoryStuff.setOnDebug(true);
                    sender.sendMessage(ChatColor.GREEN + "Debug mode enabled.");
                    GoodLogger.info("Debug mode enabled by " + sender.getName());
                }
                if(
                        args[1].equalsIgnoreCase("off") || args[1].equalsIgnoreCase("0") ||
                        args[1].equalsIgnoreCase("false") || args[1].equalsIgnoreCase("stop")
                ){
                    if(!NahumInventoryStuff.getOnDebug()){
                        sender.sendMessage(ChatColor.YELLOW + "Debug mode was already disabled.");
                        return true;
                    }

                    NahumInventoryStuff.setOnDebug(false);
                    sender.sendMessage(ChatColor.GREEN + "Debug mode disabled.");
                    GoodLogger.info("Debug mode disabled by " + sender.getName());
                }
        }
        return true;
    }

     @Override
     public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args){
        List<String> completions = new ArrayList<>();
        String currentInput = null;
        if(args.length == 1){
            if(sender.hasPermission("nahum.nahumstuff")){
                completions.add("debug");
                completions.add("backup");
                completions.add("restore");
                completions.removeIf(option -> !option.toLowerCase().startsWith(args[0].toLowerCase()));
                return completions;
            }
            if(sender.hasPermission("nahum.nahumstuff.debug")) {
                completions.add("debug");
            }
            if(sender.hasPermission("nahum.nahumstuff.restore")) {
                completions.add("restore");
            }
            if(sender.hasPermission("nahum.nahumstuff.backup")) {
                completions.add("backup");
            }
            currentInput = args[0].toLowerCase();
        }

        if(currentInput == null){
            return new ArrayList<>();
        }

        String unmatched = currentInput;
        completions.removeIf(option -> !option.toLowerCase().startsWith(unmatched));
        return completions;
     }

    public String getBackupFileName(String input){
        File backupFolder = FileManager.getBackupFolder();
        if(backupFolder == null){
            GoodLogger.error("Backup folder is null");
            return null;
        }
        Path path = Paths.get(backupFolder.toString(), input);

        if(Files.exists(path) && Files.isRegularFile(path)){
            return input;
        }

        if (!input.endsWith(".nahumbackup")) {
            Path withExtension = Paths.get(backupFolder.toString(), input + ".nahumbackup");
            if (Files.exists(withExtension) && Files.isRegularFile(withExtension)) {
                return input + ".nahumbackup";
            }
        }

        GoodLogger.error("Couldn't find backup file: " + input);
        return null;
    }

}