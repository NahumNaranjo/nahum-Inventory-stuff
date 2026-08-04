package dev.nahum.nahumInventoryStuff;

import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public final class NahumInventoryStuff extends JavaPlugin {


    private static NahumInventoryStuff plugin;
    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getCommand("echesttools").setExecutor(new EchestTools());
        this.getCommand("inventorytools").setExecutor(new InventoryTools());
        this.getCommand("inventoryclear").setExecutor(new InventoryClear());
        plugin = this;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    static public NahumInventoryStuff getInstance() {return plugin;}
}