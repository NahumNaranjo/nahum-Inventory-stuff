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
    public void dev(String message) {
        boolean onDev = (boolean)ConfigManager.getConfigOrDefault("onDev", false);
        if(onDev){
            sender.sendMessage(ChatColor.ITALIC + "NIS-DEV: " + message);
        }
    }
    public void time(String message) {
        boolean timeInfo = (boolean)ConfigManager.getConfigOrDefault("timeInfo", false);
        if(timeInfo){
            sender.sendMessage(ChatColor.ITALIC + "NIS-TIME: " + message);
        }
    }
    public void web(String message) {
        boolean webInfo = (boolean)ConfigManager.getConfigOrDefault("webInfo", false);
        if(webInfo){
            sender.sendMessage(ChatColor.ITALIC + "NIS-WEB: " + message);
        }
    }
}
