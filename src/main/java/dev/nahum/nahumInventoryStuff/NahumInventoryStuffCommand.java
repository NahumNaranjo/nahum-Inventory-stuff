package dev.nahum.nahumInventoryStuff;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.*;

public class NahumInventoryStuffCommand implements TabExecutor {
    private String[] configNames = {
            "all", "autoBackup", "fixedMode", "lapse", "schedule", "onDebug", "maxDeathSnapshots",
            "maxJoinSnapshots", "maxLeaveSnapshots", "maxChangeSnapshots", "maxForcedSnapshots",
            "maxPlayers", "maxSnapshots", "autoDelete", "deleteOlderThan"
    };
    private String[] features = {
            "debug", "backup", "restore", "config", "undo"
    };

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        NISCommandTools tools = new NISCommandTools(sender);
        SenderLogger toSender = new  SenderLogger(sender);
        if (args.length == 0) {
            toSender.success(NahumInventoryStuff.getCredits());
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "restore":
                tools.restore(args[1]);
                return true;
            case "backup":
                tools.backup(args[1]);
                break;
            case "debug":
                tools.debug(args[1]);
                break;
            case "time":
                break;
            case "undo":
                tools.undo(args[1]);
                break;

            case "config":
                if (args.length == 2 && args[1].equalsIgnoreCase("see")) {
                    showAllConfigs(sender);
                    return true;
                }
                if (args.length == 2 || (args.length < 4 && args[1].equalsIgnoreCase("set"))) {
                    toSender.error("Not enough arguments!");
                    return true;
                }
                if (args[1].equalsIgnoreCase("see") && args[2].equalsIgnoreCase("all")) {
                    showAllConfigs(sender);
                    return true;
                }

                ConfigDatum config = tools.config(args[1], args[2], args[3]);

                toSender.success(config.getName() + ":\nValue: " + config.getValue().toString() + "\n Path: " + config.getPath());
                ConfigManager.reload();
                return true;

        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        String currentInput = null;
        if (args.length == 1) {
            if (sender.hasPermission("nahum.nahumstuff")) {
                completions.addAll(Arrays.asList(features));
                completions.removeIf(option -> !option.toLowerCase().startsWith(args[0].toLowerCase()));
                return completions;
            }
            if (sender.hasPermission("nahum.nahumstuff.debug")) {
                completions.add("debug");
            }
            if (sender.hasPermission("nahum.nahumstuff.restore")) {
                completions.add("restore");
            }
            if (sender.hasPermission("nahum.nahumstuff.backup")) {
                completions.add("backup");
            }
            if (sender.hasPermission("nahum.nahumstuff.config.undo")) {
                completions.add("undo");
            }
            if (sender.hasPermission("nahum.nahumstuff.config")) {
                completions.add("config");
            }
            currentInput = args[0].toLowerCase();
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("undo")) {
            List<String> players = new LinkedList<>();
            for (OfflinePlayer player : OfflinePlayerSync.getAllPlayers()) {
                players.add(player.getName());
            }
            Collections.sort(players);
            players.removeIf(option -> !option.toLowerCase().startsWith(args.length == 2 ? args[1] : args[2]));
            return players;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("config")) {
            completions.add("see");
            if (sender.hasPermission("nahum.nahumstuff.config.set")) {
                completions.add("set");
            }
            currentInput = args[1].toLowerCase();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("config")) {
            completions.addAll(Arrays.asList(configNames));
            currentInput = args[2].toLowerCase();
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("config") && sender.hasPermission("nahum.nahumstuff.config.set") && !args[2].equalsIgnoreCase("all")) {
            String last = args[2];
            String[] booleans = {"autoBackup", "fixedMode", "onDebug",};
            for (String option : booleans) {
                if (option.toLowerCase().equals(last)) {
                    completions.add("true");
                    completions.add("false");
                }
            }
            currentInput = args[3].toLowerCase();
        }

        if (currentInput == null) {
            return new ArrayList<>();
        }

        String unmatched = currentInput;
        completions.removeIf(option -> !option.toLowerCase().startsWith(unmatched));
        return completions;
    }

    public void showAllConfigs(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "NahumInventoryStuff configs: ");
        Map<String, Object> map = ConfigManager.getAllConfigs();
        for (String k : map.keySet()) {
            sender.sendMessage(ChatColor.GREEN + "   -" + k + ": " + map.get(k).toString());
        }
    }

}