package dev.nahum.nahumInventoryStuff;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class NahumInventoryStuff extends JavaPlugin {

    private final static String version = "1.2.0";
    private final static String date = "05/August/26";
    private static NahumInventoryStuff plugin;

    public static boolean onDebug = false;

    public static String getVVersion(){
        return "v" + version;
    }
    public static String getFullVVersion(){
        return "NahumInventoryStuff v" + version;
    }
    public static String getVersion(){
        return version;
    }
    public static String getFullVersion(){return "NahumInventoryStuff " + version;}
    public static String getCredits(){
        return "NahumInventoryStuff :D\n" +
                "Author: Nahum Naranjo\n" +
                "Version: " + version + "\n" +
                "Date of publishing: " + date;
    }
    static public NahumInventoryStuff getInstance() {return plugin;}
    static public Logger getLog() {return GoodLogger.getLog();}
    static public Logger getGoodLogger() {return plugin != null ? plugin.getLogger() : Bukkit.getLogger();}
    static public boolean getOnDebug() {return onDebug;}
    static public void setOnDebug(boolean newDebug) {onDebug = newDebug;}

    @Override
    public void onEnable() {
        plugin = this;
        GoodLogger.success(getFullVersion() + " enabled");
        GoodLogger.info("Debug mode: " + (onDebug ? "ON" : "OFF") + " (toggle with /nahumstuff debug)");
        GoodLogger.debug("Registering command executors...");

        this.getCommand("echesttools").setExecutor(new EchestTools());
        this.getCommand("inventorytools").setExecutor(new InventoryTools());
        this.getCommand("inventoryclear").setExecutor(new InventoryClear());
        this.getCommand("ec").setExecutor(new EcCommand());
        this.getCommand("inv").setExecutor(new InvCommand());
        this.getCommand("nahumstuff").setExecutor(new NahumInventoryStuffCommand());

        GoodLogger.debug("World folder: " + Bukkit.getWorlds().getFirst().getWorldFolder().getAbsolutePath());
        GoodLogger.debug("Plugin data folder: " + getDataFolder().getAbsolutePath());
    }

    @Override
    public void onDisable() {
        GoodLogger.info(getFullVersion() + " disabled");
    }

}