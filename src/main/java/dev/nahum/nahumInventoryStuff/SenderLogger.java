package dev.nahum.nahumInventoryStuff;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Objects;

public class SenderLogger {
    CommandSender sender;
    public SenderLogger(CommandSender sender) {
        this.sender = Objects.requireNonNull(sender);
    }
    public void success(String message) {
        sender.sendMessage(ChatColor.GREEN + message);
    }
    public void error(String message) {
        sender.sendMessage(ChatColor.RED + message);
    }
    public void info(String message) {
        sender.sendMessage(ChatColor.GOLD + message);
    }
    public void warning(String message) {
        sender.sendMessage(ChatColor.YELLOW + message);
    }
    public void debug(String message) {
        boolean onDebug = (boolean)ConfigManager.getConfigOrDefault("onDebug", false);
        if(onDebug)
            sender.sendMessage(ChatColor.ITALIC + "NIS DEBUG: " + message);
    }
}
