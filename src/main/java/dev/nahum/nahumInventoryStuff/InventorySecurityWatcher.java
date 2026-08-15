package dev.nahum.nahumInventoryStuff;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public class InventorySecurityWatcher implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        Inventory clickedInventory = event.getClickedInventory();

        if(clickedInventory == null) return;

        if (!(event.getWhoClicked() instanceof Player admin)) return;

        if(NahumInventoryStuff.isOnAdminWatchList(admin)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event){
        if(!(event.getWhoClicked() instanceof Player admin)) return;

        if(NahumInventoryStuff.isOnAdminWatchList(admin)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        NahumInventoryStuff.removeFromAdminWatchList(event.getPlayer().getUniqueId());
    }
}
