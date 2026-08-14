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
                    seeInventory(targetPlayer, viewer, sender);
                    GoodLogger.info(sender.getName() + " is looking at " +
                            targetPlayer.getName() + "'s inventory using the command /inventorytools with the argument \"see\"!");
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
        if(args.length == 2) {
            List<String> players = new LinkedList<>();
            for(OfflinePlayer player : OfflinePlayerSync.getAllPlayers()){
                players.add(player.getName());
            }
            Collections.sort(players);
            players.removeIf(option -> !option.toLowerCase().startsWith(args[1]));
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
            player.getPlayer().getInventory().clear();
        } else{
            try{
                CompoundTag rootTag = FileManager.getPlayerData(player.getUniqueId());
                File playerData = FileManager.getPlayerFile(player.getUniqueId());

                if(rootTag == null){
                    sender.sendMessage(ChatColor.RED + player.getName() + "'s player data is not a valid file!");
                    return false;
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
                recipient.getPlayer().getInventory().setContents(giverPlayer.getPlayer().getInventory().getContents());
                giverPlayer.getPlayer().getInventory().setContents(helper);
            } else {
                recipient.getPlayer().getInventory().setContents(giverPlayer.getPlayer().getInventory().getContents());
            }
        } else{
            try{
                File recipientData = FileManager.getPlayerFile(recipient.getUniqueId());
                CompoundTag giverTag = FileManager.getPlayerData(giverPlayer.getUniqueId());
                CompoundTag rootTag = FileManager.getPlayerData(recipient.getUniqueId());

                if(rootTag == null){
                    GoodLogger.warn(recipient.getName() + "'s player data is not a valid file!");
                    return true;
                }

                if(giverTag == null){
                    GoodLogger.warn(giverName + "'s player data is not a valid file!");
                    return true;
                }

                Optional<ListTag> list =  giverTag.getList(NbtTags.getInventory());
                if(list.isEmpty()){
                    GoodLogger.warn(giverName + "'s Inventory is empty!");
                    return true;
                }

                ListTag listTag = list.get();
                if(mode == 0){
                    ListTag newListTag = rootTag.getListOrEmpty(NbtTags.getInventory());
                    if(!newListTag.isEmpty()){
                        giverTag.put(NbtTags.getInventory(), newListTag);
                        NbtIo.writeCompressed(giverTag, FileManager.getPlayerFile(giverPlayer.getUniqueId()).toPath());
                    }
                }

                rootTag.put(NbtTags.getInventory(), listTag);
                NbtIo.writeCompressed(rootTag, recipientData.toPath());
            } catch (Exception e){
                GoodLogger.error(recipient.getName() + "'s player data is not a valid file!");
                return true;
            }
        }

        sender.sendMessage(ChatColor.GREEN + "Successfully transferred " + giverPlayer.getName() +
                "'s inventory to " + recipient.getName() + "'s!");
        return true;
    }

    public static boolean seeInventory(OfflinePlayer targetPlayer, Player viewer, CommandSender sender){
        // Create a copy of the ender chest to prevent modifications
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
        viewer.openInventory(inventory);
        return true;
    }
    public static boolean consoleSee(){
        return true;
    }
}