package dev.nahum.nahumInventoryStuff;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class InventorySecurityWatcher implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        Inventory clickedInventory = event.getClickedInventory();

        if(clickedInventory == null) return;

        if (!(event.getWhoClicked() instanceof Player admin)) return;

        if(NahumInventoryStuff.isOnIsEditingList(admin)) return;

        if(NahumInventoryStuff.isOnAdminWatchList(admin)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event){
        if(!(event.getWhoClicked() instanceof Player admin)) return;

        if(NahumInventoryStuff.isOnIsEditingList(admin)) return;

        if(NahumInventoryStuff.isOnAdminWatchList(admin)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        GoodLogger.debug("Closed an inventory");
        UUID uuid = event.getPlayer().getUniqueId();
        GoodLogger.debug("Got " + uuid);
        if(NahumInventoryStuff.isOnAdminWatchList(uuid)){
            NahumInventoryStuff.removeFromAdminWatchList(uuid);
            GoodLogger.debug("removed admin watch list");
        }
        if(NahumInventoryStuff.isOnIsEditingList(uuid)){
            GoodLogger.debug("was editing");
            if(event.getInventory().getType() == InventoryType.ENDER_CHEST){
                GoodLogger.debug("Closed echest");
                FileManager.saveEchest(event.getInventory().getContents(), NahumInventoryStuff.getVictimFromIsEditingList(uuid));
                GoodLogger.debug("saved echest");
            } else if(event.getInventory().getType()  == InventoryType.PLAYER){
                GoodLogger.debug("Closed inventory");
                FileManager.saveInventory(event.getInventory().getContents(), NahumInventoryStuff.getVictimFromIsEditingList(uuid));
                GoodLogger.debug("saved inventory");
            }
            NahumInventoryStuff.removeFromIsEditingList(uuid, null);
            GoodLogger.debug("removed from is editing");
        }

    }
}
