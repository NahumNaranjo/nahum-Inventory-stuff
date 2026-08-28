package dev.nahum.nahumInventoryStuff;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.nio.file.Path;

public class InvTools {
    public PlayerDataReader reader;
    public PlayerDataWriter writer;
    public SnapshotManager manager;
    public SenderLogger toSender;
    public CommandSender sender;
    public InvTools(PlayerDataReader reader,PlayerDataWriter writer, SnapshotManager manager, CommandSender sender) {
        this.reader = reader;
        this.writer = writer;
        this.toSender = new SenderLogger(sender);
        this.sender = sender;
        this.manager = manager;
    }
    public InvTools(CommandSender sender){
        this.toSender = new SenderLogger(sender);
        this.sender = sender;
        this.reader = new PlayerDataReader();
        this.writer = new PlayerDataWriter(this.reader);
        this.manager = new SnapshotManager(this.reader, this.writer);
    }

    private String getObjectPlayerName(Object player){
        String playerName;
        switch(OfflinePlayerSync.getPlayerType(player)) {
            case "player" -> playerName = ((Player)player).getName();
            case "offlineplayer" -> playerName = ((OfflinePlayer)player).getName();
            default -> playerName = "console";
        }
        return playerName;
    }

    private void performAdminBufferSave(Object player, Object inv, Object player2, Object inv2){
        String path1 = performAdminBufferSave(player, inv, "swapped");
        String path2 = performAdminBufferSave(player2, inv2, "swapped");
        manager.updateSnapshot(Path.of(path1), player, path2, NbtTags.getLinkedTo());
        manager.updateSnapshot(Path.of(path2), player2, path1, NbtTags.getLinkedTo());
    }

    private String performAdminBufferSave(Object player, Object inv, String verb) {
        ItemStack[] stack = DataParser.objectToItemStackArr(inv);
        String playerName = getObjectPlayerName(player);

        return manager.performAdminBufferSave(
                sender,
                player,
                stack,
                null,
                sender.getName() + verb + playerName + "'s inventory!",
                "maxSnapshot",
                null
        );
    }

    public boolean cleanInventory(OfflinePlayer player) {
        if (player.getPlayer() != null) {
            ItemStack[] contents = player.getPlayer().getInventory().getContents();
            performAdminBufferSave(player, contents, "cleared");

            player.getPlayer().getInventory().clear();
        } else {
            CompoundTag rootTag = reader.getPlayerData(player.getUniqueId());

            if (rootTag == null) {
                toSender.error(player.getName() + "'s player data is not a valid file!");
                GoodLogger.debug(player.getName() + "'s player data is not a valid file!");
                return false;
            }

            ListTag oldInv = rootTag.getListOrEmpty(NbtTags.getInventory());
            performAdminBufferSave(player, oldInv, "cleared");

            rootTag.put(NbtTags.getInventory(), new ListTag());

            try {
                NbtIo.writeCompressed(rootTag, PathManager.getPlayerFile(player.getUniqueId()).toPath());
            } catch (Exception e) {
                toSender.error(player.getName() + " couldn't write to player's data file!");
                return false;
            }
        }
        toSender.success("Successfully cleared " + player.getName() + "'s inventory!");
        return true;
    }

    // mode = 0 for swap, any other value will result in a transfer
    public boolean swapInventory(OfflinePlayer recipient, OfflinePlayer giverPlayer){
        return transferInventory(recipient, giverPlayer, (byte)0);
    }

    public boolean transferInventory(OfflinePlayer recipient, OfflinePlayer giverPlayer, byte mode) {
        String giverName = giverPlayer.getName();

        if (OfflinePlayerSync.isOnline(giverPlayer) && OfflinePlayerSync.isOnline(recipient)) {
            if (mode == 0) {
                ItemStack[] recipientFirstInv =  recipient.getPlayer().getInventory().getContents();
                ItemStack[] giverFirstInv = giverPlayer.getPlayer().getInventory().getContents();

                performAdminBufferSave(recipient, recipientFirstInv, giverPlayer, giverFirstInv);

                recipient.getPlayer().getInventory().setContents(giverFirstInv);
                giverPlayer.getPlayer().getInventory().setContents(recipientFirstInv);
            } else {
                performAdminBufferSave(recipient, recipient.getPlayer().getInventory().getContents(), "transferred");

                recipient.getPlayer().getInventory().setContents(giverPlayer.getPlayer().getInventory().getContents());
            }

        } else {
            try {
                CompoundTag giverTag = reader.getPlayerData(giverPlayer.getUniqueId());
                CompoundTag recipientTag = reader.getPlayerData(recipient.getUniqueId());

                if (recipientTag == null) {
                    GoodLogger.warn(recipient.getName() + "'s player data is not a valid file!");
                    return false;
                }
                if (giverTag == null) {
                    GoodLogger.warn(giverName + "'s player data is not a valid file!");
                    return false;
                }

                ItemStack[] recipientInv = Serializer.buildFullInventoryFromPlayerTag(recipientTag);
                ItemStack[] giverInv = Serializer.buildFullInventoryFromPlayerTag(giverTag);

                if (recipient.isOnline()) {
                    recipientInv = recipient.getPlayer().getInventory().getContents();
                }
                if (giverPlayer.isOnline()) {
                    giverInv = giverPlayer.getPlayer().getInventory().getContents();
                }

                if (mode == 0) {
                    if (giverPlayer.isOnline()) {
                        Player online = giverPlayer.getPlayer();
                        writer.pasteInventory(recipientInv, online);
                    } else {
                        writer.saveInventory(recipientInv, giverPlayer);
                    }
                    performAdminBufferSave(recipient, recipientInv, giverPlayer, giverInv);
                } else {
                    performAdminBufferSave(recipient, recipientInv, "transferred");
                }

                if (recipient.isOnline()) {
                    Player online = recipient.getPlayer();
                    writer.pasteInventory(giverInv, online);
                } else {
                    writer.saveInventory(giverInv, recipient);
                }

            } catch (Exception e) {
                GoodLogger.error(recipient.getName() + "'s player data is not a valid file!");
                e.printStackTrace();
                return false;
            }
        }

        toSender.success("Successfully " + (mode == 0 ? "swaped " : "transferred ") + giverPlayer.getName() +
                "'s inventory to " + recipient.getName() + "'s!");
        return true;
    }

    public boolean seeInventory(OfflinePlayer targetPlayer, Player viewer) {
        Inventory inventory = Bukkit.createInventory(null, InventoryType.PLAYER, ChatColor.BOLD + targetPlayer.getName() + "'s Inventory");
        if (targetPlayer.isOnline()) {
            inventory.setContents(targetPlayer.getPlayer().getInventory().getContents());
            viewer.openInventory(inventory);
        } else {
            try {
                CompoundTag rootTag = reader.getPlayerData(targetPlayer.getUniqueId());
                if (rootTag == null) {
                    toSender.error(targetPlayer.getName() + "'s data file is empty!");
                    return true;
                }

                ItemStack[] playerInv = Serializer.buildFullInventoryFromPlayerTag(rootTag);
                inventory.setContents(playerInv);

                viewer.openInventory(inventory);
            } catch (Exception e) {
                toSender.error(targetPlayer.getName() + "'s player data is not a valid file!");
                GoodLogger.debug(targetPlayer.getName() + "'s player data is not a valid file!" + "\n" + e.getMessage());
            }
        }
        return true;
    }

    public boolean editInventory(OfflinePlayer targetPlayer, Player viewer) {
        Inventory inventory = Bukkit.createInventory(null, InventoryType.PLAYER, ChatColor.BOLD + targetPlayer.getName() + "'s Inventory");
        if (targetPlayer.isOnline()) {
            inventory = targetPlayer.getPlayer().getInventory();
            performAdminBufferSave(targetPlayer, inventory, "edited");
            GoodLogger.debug(targetPlayer.getName() + " is editing " + targetPlayer.getName() + "'s inventory, snapshoted!");
            NahumInventoryStuff.addToIsEditingList(viewer,  targetPlayer);
            viewer.openInventory(targetPlayer.getPlayer().getInventory());
        } else {
            try {
                CompoundTag rootTag = reader.getPlayerData(targetPlayer.getUniqueId());
                if (rootTag == null) {
                    toSender.error(targetPlayer.getName() + "'s data file is empty!");
                    return true;
                }

                ItemStack[] playerInv = Serializer.buildFullInventoryFromPlayerTag(rootTag);
                inventory.setContents(playerInv);

                performAdminBufferSave(targetPlayer, playerInv, "edited");
                NahumInventoryStuff.addToIsEditingList(viewer,  targetPlayer);
                viewer.openInventory(inventory);
            } catch (Exception e) {
                toSender.error(targetPlayer.getName() + "'s player data is not a valid file!");
                GoodLogger.debug(e.toString());
                e.printStackTrace();
            }
        }
        return true;
    }

    public static boolean consoleSee() {
        return true;
    }
}
