package dev.nahum.nahumInventoryStuff;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.ChatColor;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

public class InventoryTools implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }

        // Check for "menu" (single argument)
        if (args.length == 1) {
            // menu logic
            return true;
        }

        // Commands requiring at least 2 arguments
        if (args.length >= 2) {
            String option = args[0].toLowerCase();
            String targetName = args[1];

            // Check if target is online
            OfflinePlayer targetPlayer = OfflinePlayerSync.getPlayer(targetName, sender);
            if (targetPlayer == null) {
                return true;
            }

            switch (option) {
                case "see":
                    if(!sender.hasPermission("nahum.inventorytools.see") && !sender.hasPermission("nahum.inventorytools")){
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                        return true;
                    }
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
                        return true;
                    }

                    Player viewer = (Player) sender;
                    NahumInventoryStuff.addToAdminWatchList(viewer);
                    seeInventory(targetPlayer, viewer, sender);
                    GoodLogger.info(sender.getName() + " is looking at " +
                            targetPlayer.getName() + "'s inventory using the command /inventorytools with the argument \"see\"!");
                    break;
                case "edit":
                    if(!sender.hasPermission("nahum.inventorytools.edit") && !sender.hasPermission("nahum.inventorytools")){
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                        return true;
                    }
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
                        return true;
                    }
                    viewer = (Player) sender;
                    NahumInventoryStuff.addToIsEditingList(viewer, targetPlayer);
                    GoodLogger.debug("Added " + viewer.getUniqueId() + " to is editing");
                    editInventory(targetPlayer, viewer, sender);
                    GoodLogger.info(sender.getName() + " is editing at " +
                            targetPlayer.getName() + "'s inventory using the command /inventorytools with the argument \"edit\"!");
                    break;

                case "transfer":
                    if(!sender.hasPermission("nahum.inventorytools.transfer") && !sender.hasPermission("nahum.inventorytools")){
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                        return true;
                    }
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "Usage: /inventorytools transfer <recipient> <giver>");
                        return true;
                    }

                    OfflinePlayer giverPlayer = OfflinePlayerSync.getPlayer(args[2], sender);
                    if (giverPlayer == null) {
                        sender.sendMessage(ChatColor.RED + "Couldn't get " + args[2] + "'s OfflinePlayer!");
                        return true;
                    }

                    transferInventory(targetPlayer, giverPlayer, sender, (byte)1);
                    GoodLogger.info(sender.getName() + " transfered " +
                            targetPlayer.getName() + "'s inventory to " + giverPlayer.getName() +
                            "'s using the command /inventorytools with the argument \"transfer\"!");
                    break;

                case "clear":
                    if(!sender.hasPermission("nahum.inventorytools.clear") && !sender.hasPermission("nahum.inventorytools")){
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                        return true;
                    }
                    cleanInventory(targetPlayer, sender);
                    GoodLogger.info(sender.getName() + " has cleared " +
                            targetPlayer.getName() + "'s inventory using the command /inventorytools with the argument \"clear\"!");
                    break;

                case "swap":
                    if(!sender.hasPermission("nahum.inventorytools.swap") && !sender.hasPermission("nahum.inventorytools")){
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                        return true;
                    }
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "Usage: /inventorytools transfer <recipient> <giver>");
                        return true;
                    }
                    OfflinePlayer swapPlayer = OfflinePlayerSync.getPlayer(args[2], sender);
                    if (swapPlayer == null) {
                        sender.sendMessage(ChatColor.RED + "Couldn't get " + args[2] + "'s OfflinePlayer!");
                        return true;
                    }
                    transferInventory(targetPlayer, swapPlayer, sender, (byte)0);
                    GoodLogger.info(sender.getName() + " transfered " +
                            targetPlayer.getName() + "'s inventory to " + swapPlayer.getName() +
                            "'s using the command /inventorytools with the argument \"transfer\"!");
                    return true;


                default:
                    sender.sendMessage(ChatColor.RED + "Unknown option: " + option);
                    sender.sendMessage(ChatColor.RED + "Valid options: see, transfer, clear");
                    return false;
            }
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        String currentInput = null;
        if(args.length == 1) {
            if(sender.hasPermission("nahum.inventorytools")){
                completions.add("clear");
                completions.add("swap");
                completions.add("transfer");
                completions.add("see");
                completions.removeIf(option -> !option.toLowerCase().startsWith(args[0].toLowerCase()));
                return completions;
            }
            if(sender.hasPermission("nahum.inventorytools.see")){
                completions.add("see");
            }
            if(sender.hasPermission("nahum.inventorytools.swap")){
                completions.add("swap");
            }
            if(sender.hasPermission("nahum.inventorytools.transfer")){
                completions.add("transfer");
            }
            if(sender.hasPermission("nahum.inventorytools.clear")){
                completions.add("clear");
            }
            currentInput = args[0].toLowerCase();
        }
        if(args.length == 2 || args.length == 3) {
            List<String> players = new LinkedList<>();
            for(OfflinePlayer player : OfflinePlayerSync.getAllPlayers()){
                players.add(player.getName());
            }
            Collections.sort(players);
            players.removeIf(option -> !option.toLowerCase().startsWith(args.length == 2 ? args[1] : args[2]));
            return players;
        }
        if(currentInput == null) {
            return new ArrayList<>();
        }
        String unmatched = currentInput;
        completions.removeIf(option -> !option.toLowerCase().startsWith(unmatched));
        return completions;
    }

    public static boolean cleanInventory(OfflinePlayer player, CommandSender sender){
        if(player.getPlayer() != null){
            ItemStack[] contents = player.getPlayer().getInventory().getContents();
            if(sender instanceof Player senderPlayer){
                FileManager.performAdminBufferSave(
                        senderPlayer,
                        player,
                        contents,
                        null,
                        senderPlayer.getName() + " cleared " + player.getName() + "'s inventory!",
                        "maxSnapshot",
                        null
                );
            } else {
                FileManager.performAdminBufferSave(
                        "console",
                        player,
                        contents,
                        null,
                        "Console cleared " + player.getName() + "'s inventory!",
                        "maxSnapshot",
                        null
                );
            }
            player.getPlayer().getInventory().clear();
        } else{
            try{
                CompoundTag rootTag = FileManager.getPlayerData(player.getUniqueId());
                File playerData = FileManager.getPlayerFile(player.getUniqueId());

                if(rootTag == null){
                    sender.sendMessage(ChatColor.RED + player.getName() + "'s player data is not a valid file!");
                    return false;
                }

                ListTag oldInv = rootTag.getListOrEmpty(NbtTags.getInventory());
                if(sender instanceof Player senderPlayer){
                    FileManager.performAdminBufferSave(
                            senderPlayer,
                            player,
                            Serializer.deserializeFromListTag(oldInv, Serializer.MAIN_INVENTORY_SIZE),
                            null,
                            senderPlayer.getName() + " cleared " + player.getName() + "'s inventory!",
                            "maxSnapshot",
                            null
                    );
                } else {
                    FileManager.performAdminBufferSave(
                            "console",
                            player,
                            Serializer.deserializeFromListTag(oldInv, Serializer.MAIN_INVENTORY_SIZE),
                            null,
                            "Console cleared " + player.getName() + "'s inventory!",
                            "maxSnapshot",
                            null
                    );
                }
                rootTag.put(NbtTags.getInventory(), new ListTag());
                NbtIo.writeCompressed(rootTag, playerData.toPath());
            } catch (Exception e){
                sender.sendMessage(ChatColor.RED + player.getName() + "'s player data is not a valid file!");
                return false;
            }
        }
        sender.sendMessage(ChatColor.GREEN + "Successfully cleared " + player.getName() + "'s inventory!");
        return true;
    }
    public static boolean cleanInventoryNoSend(OfflinePlayer player){
        if(player.getPlayer() != null){
            player.getPlayer().getInventory().clear();
        } else{
            try{
                CompoundTag rootTag = FileManager.getPlayerData(player.getUniqueId());
                File playerData = FileManager.getPlayerFile(player.getUniqueId());

                if(rootTag == null){
                    return false;
                }
                rootTag.put(NbtTags.getInventory(), new ListTag());
                NbtIo.writeCompressed(rootTag, playerData.toPath());
            } catch (Exception e){
                return false;
            }
        }
        return true;
    }

    // mode = 0 for swap, any other value will result in a transfer
    public static boolean transferInventory(OfflinePlayer recipient, OfflinePlayer giverPlayer, CommandSender sender, byte mode){
        String giverName = giverPlayer.getName();

        if(OfflinePlayerSync.isOnline(giverPlayer) && OfflinePlayerSync.isOnline(recipient)){
            if(mode == 0){
                ItemStack[] helper = recipient.getPlayer().getInventory().getContents();
                saveSwapData(sender, recipient.getPlayer(), giverPlayer);
                recipient.getPlayer().getInventory().setContents(giverPlayer.getPlayer().getInventory().getContents());
                giverPlayer.getPlayer().getInventory().setContents(helper);
            } else {
                recipient.getPlayer().getInventory().setContents(giverPlayer.getPlayer().getInventory().getContents());
                saveTransferData(sender, recipient, recipient.getPlayer().getInventory().getContents(), giverName);
            }
        } else {
            try{
                CompoundTag giverTag = FileManager.getPlayerData(giverPlayer.getUniqueId());
                CompoundTag recipientTag = FileManager.getPlayerData(recipient.getUniqueId());
                GoodLogger.debug("got to save all vars");

                if(recipientTag == null){
                    GoodLogger.warn(recipient.getName() + "'s player data is not a valid file!");
                    return false;
                }   
                if(giverTag == null){
                    GoodLogger.warn(giverName + "'s player data is not a valid file!");
                    return false;
                }

                ListTag listTag = giverTag.getListOrEmpty(NbtTags.getInventory());
                GoodLogger.debug("got list");
                if(mode == 0){
                    FileManager.saveInventory(Serializer.buildFullInventoryFromPlayerTag(recipientTag), giverPlayer);
                    GoodLogger.debug("wrote list2");
                    saveSwapData(sender, recipient, giverPlayer);
                    GoodLogger.debug("backd list2");
                }
                if(mode != 0) {
                    saveTransferData(sender, recipient, Serializer.buildFullInventoryFromPlayerTag(recipientTag), giverName);
                    GoodLogger.debug("backd list1");
                }

                recipientTag.put(NbtTags.getInventory(), listTag);
                GoodLogger.debug("put list1");
                FileManager.saveInventory(Serializer.buildFullInventoryFromPlayerTag(giverTag), recipient);
                GoodLogger.debug("wrote list1");
            } catch (Exception e){
                GoodLogger.error(recipient.getName() + "'s player data is not a valid file!");
                e.printStackTrace();
                return false;
            }
        }

        sender.sendMessage(ChatColor.GREEN + "Successfully transferred " + giverPlayer.getName() +
                "'s inventory to " + recipient.getName() + "'s!");
        return true;
    }


    public static boolean seeInventory(OfflinePlayer targetPlayer, Player viewer, CommandSender sender){
        Inventory inventory = Bukkit.createInventory(null, InventoryType.PLAYER, ChatColor.BOLD + targetPlayer.getName() + "'s Inventory");
        if(targetPlayer.getPlayer() != null){
            inventory.setContents(targetPlayer.getPlayer().getInventory().getContents());
            viewer.openInventory(inventory);
        } else{
            try{
                CompoundTag rootTag = FileManager.getPlayerData(targetPlayer.getUniqueId());
                if(rootTag == null){
                    sender.sendMessage(ChatColor.RED + targetPlayer.getName() + "'s data file is empty!");
                    return true;
                }

                Optional<ListTag> inventoryList = rootTag.getList(NbtTags.getInventory());
                if(inventoryList.isEmpty()){
                    sender.sendMessage(ChatColor.RED + targetPlayer.getName() + "'s Inventory is empty!");
                    return true;
                }

                ListTag inventoryListTag = inventoryList.get();
                for(int i = 0; i < inventoryListTag.size(); i++){
                    CompoundTag itemTag = inventoryListTag.getCompoundOrEmpty(i);
                    if(itemTag.contains(NbtTags.getSlot()) && itemTag.contains(NbtTags.getId())){

                        Optional<Byte> slot = itemTag.getByte(NbtTags.getSlot());
                        Optional<Integer> count = itemTag.getInt(NbtTags.getCount());
                        Optional<String> id = itemTag.getString(NbtTags.getId());

                        if(slot.isPresent() && count.isPresent() && id.isPresent()){
                            Material material = Material.matchMaterial(id.get());
                            if(material == null){
                                sender.sendMessage(ChatColor.RED + targetPlayer.getName() + "'s invalid id found, check for corrupted files!");
                                return true;
                            }
                            ItemStack item = new ItemStack(material, count.get());
                            if(itemTag.contains(NbtTags.getComponents())){
                                Optional<CompoundTag> components = itemTag.getCompound(NbtTags.getComponents());
                                Bukkit.getUnsafe().modifyItemStack(item, itemTag.toString());
                            }
                            inventory.setItem(slot.get(), item);
                        } else {
                            sender.sendMessage(ChatColor.RED + "Couldn't parse the user's data");
                            return true;
                        }
                    }
                }
                viewer.openInventory(inventory);
            } catch (Exception e){
                sender.sendMessage(ChatColor.RED + targetPlayer.getName() + "'s player data is not a valid file!");
            }
        }
        return true;
    }
    public static boolean editInventory(OfflinePlayer targetPlayer, Player viewer, CommandSender sender){
        Inventory inventory = Bukkit.createInventory(null, InventoryType.PLAYER, ChatColor.BOLD + targetPlayer.getName() + "'s Inventory");
        if(targetPlayer.getPlayer() != null){
            inventory = targetPlayer.getPlayer().getInventory();
            saveEditData(viewer, targetPlayer, inventory);
            GoodLogger.debug(targetPlayer.getName() + " is editing " + targetPlayer.getName() + "'s inventory, snapshoted!");
            viewer.openInventory(targetPlayer.getPlayer().getInventory());
        } else{
            try{
                CompoundTag rootTag = FileManager.getPlayerData(targetPlayer.getUniqueId());
                if(rootTag == null){
                    sender.sendMessage(ChatColor.RED + targetPlayer.getName() + "'s data file is empty!");
                    return true;
                }

                Optional<ListTag> inventoryList = rootTag.getList(NbtTags.getInventory());
                if(inventoryList.isEmpty()){
                    sender.sendMessage(ChatColor.RED + targetPlayer.getName() + "'s Inventory is empty!");
                    return true;
                }

                ListTag inventoryListTag = inventoryList.get();
                for(int i = 0; i < inventoryListTag.size(); i++){
                    CompoundTag itemTag = inventoryListTag.getCompoundOrEmpty(i);
                    if(itemTag.contains(NbtTags.getSlot()) && itemTag.contains(NbtTags.getId())){

                        Optional<Byte> slot = itemTag.getByte(NbtTags.getSlot());
                        Optional<Integer> count = itemTag.getInt(NbtTags.getCount());
                        Optional<String> id = itemTag.getString(NbtTags.getId());

                        if(slot.isPresent() && count.isPresent() && id.isPresent()){
                            Material material = Material.matchMaterial(id.get());
                            if(material == null){
                                sender.sendMessage(ChatColor.RED + targetPlayer.getName() + "'s invalid id found, check for corrupted files!");
                                return true;
                            }
                            ItemStack item = new ItemStack(material, count.get());
                            if(itemTag.contains(NbtTags.getComponents())){
                                Bukkit.getUnsafe().modifyItemStack(item, itemTag.toString());
                            }
                            inventory.setItem(slot.get(), item);
                        } else {
                            sender.sendMessage(ChatColor.RED + "Couldn't parse the user's data");
                            return true;
                        }
                    }
                }

                saveEditData(viewer, targetPlayer, inventory);
                viewer.openInventory(inventory);
            } catch (Exception e){
                sender.sendMessage(ChatColor.RED + targetPlayer.getName() + "'s player data is not a valid file!");
                GoodLogger.debug(e.toString());
                e.printStackTrace();
            }
        }
        return true;
    }


    public static void saveEditData(OfflinePlayer viewer, OfflinePlayer targetPlayer, Inventory inventory){
        FileManager.performAdminBufferSave(
                viewer,
                targetPlayer,
                inventory.getContents(),
                null,
                viewer.getName() + " edited " + targetPlayer.getName() + "'s inventory!",
                null,
                null
        );
    }
    public static void saveTransferData(CommandSender sender, OfflinePlayer targetPlayer, ItemStack[] inventory, String giverName){
        FileManager.performAdminBufferSave(
                ((OfflinePlayer)sender) != null ? (OfflinePlayer)sender : null,
                targetPlayer,
                inventory,
                null,
                ((Player)sender) != null ? ((Player)sender).getName() :
                        "Console" + " transferred " + giverName + "'s inventory to " + targetPlayer.getName() + "!",
                null,
                null
        );
    }
    public static void saveSwapData(CommandSender sender, OfflinePlayer player1, OfflinePlayer player2){
        String path;
        String path2;
        path = FileManager.performAdminBufferSave(
                ((OfflinePlayer)sender) != null ? (OfflinePlayer)sender : null,
                player2,
                Serializer.buildFullInventoryFromPlayerTag(FileManager.getPlayerData(player2.getUniqueId())),
                null,
                ((OfflinePlayer)sender) != null ? ((OfflinePlayer)sender).getName() : "Console" + " swapped " + player1.getName() + "'s inventory with " + player2.getName() + "!",
                null,
                null
        );
        path2 = FileManager.performAdminBufferSave(
                ((OfflinePlayer)sender) != null ? (OfflinePlayer)sender : null,
                player1,
                Serializer.buildFullInventoryFromPlayerTag(FileManager.getPlayerData(player1.getUniqueId())),
                null,
                ((OfflinePlayer)sender) != null ? ((OfflinePlayer)sender).getName() :
                        "Console" + " swapped " + player1.getName() + "'s inventory with " + player2.getName() + "!"
                ,
                null,
                path
        );
        if(path != null)
            FileManager.updateSnapshot(Paths.get(path), sender, player2, path2, "linkedTo");
    }
    public static boolean consoleSee(){
        return true;
    }
}

/*
 else if(OfflinePlayerSync.isOnline(giverPlayer) || OfflinePlayerSync.isOnline(recipient)){
            Player onlinePlayer = null;
            if(OfflinePlayerSync.isOnline(recipient)){
                onlinePlayer = recipient.getPlayer();
                ItemStack[] giverInv = FileManager.loadInventory(giverPlayer);
                if(mode == 0) {
                    saveSwapData(sender, onlinePlayer, giverPlayer);
                    FileManager.saveInventory(onlinePlayer.getInventory().getContents(), DataParser.getUuidFromObject(giverPlayer));
                }
                saveTransferData(sender, recipient, recipient.getPlayer().getInventory().getContents(), giverName);
                FileManager.saveInventory(giverInv, DataParser.getUuidFromObject(onlinePlayer));
            } else {
                onlinePlayer = giverPlayer.getPlayer();
                if(mode == 0){
                    saveSwapData(sender, onlinePlayer, recipient);
                    FileManager.pasteInventory(FileManager.loadInventory(recipient), onlinePlayer);
                }
                saveTransferData(sender, recipient, Serializer.buildFullInventoryFromPlayerTag(FileManager.getPlayerData(recipient.getUniqueId())), giverName);
                FileManager.saveInventory(onlinePlayer.getInventory().getContents(), DataParser.getUuidFromObject(recipient));
            }
        }

if (!OfflinePlayerSync.isOnline(giverPlayer) && !OfflinePlayerSync.isOnline(recipient))
 */