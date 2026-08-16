package dev.nahum.nahumInventoryStuff;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class InventorySecurityWatcher implements Listener {
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        ItemStack[] inv = e.getEntity().getPlayer().getInventory().getContents();
        ItemStack[] ec = e.getEntity().getPlayer().getEnderChest().getContents();
        String deathMessage = e.getDeathMessage();
        GoodLogger.debug(e.getEntity().getPlayer().getName() + " died, snapshotting with message: " + deathMessage);
        FileManager.performPlayerSaveSnapshot(e.getEntity().getPlayer(), inv, ec, deathMessage, "maxDeathSnapshots");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        ItemStack[] inv = e.getPlayer().getInventory().getContents();
        ItemStack[] ec = e.getPlayer().getEnderChest().getContents();
        String joinMessage = e.getJoinMessage();
        GoodLogger.debug(e.getPlayer().getName() + " joined, snapshotting with message: " + joinMessage);
        FileManager.performPlayerSaveSnapshot(e.getPlayer().getPlayer(), inv, ec, joinMessage, "maxJoinSnapshots");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        ItemStack[] inv = e.getPlayer().getInventory().getContents();
        ItemStack[] ec = e.getPlayer().getEnderChest().getContents();
        String quitMessage = e.getQuitMessage();
        GoodLogger.debug(e.getPlayer().getName() + " joined, snapshotting with message: " + quitMessage);
        FileManager.performPlayerSaveSnapshot(e.getPlayer().getPlayer(), inv, ec, quitMessage, "maxJoinSnapshots");
    }

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
