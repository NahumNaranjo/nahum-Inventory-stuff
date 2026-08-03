package dev.nahum.nahumInventoryStuff;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import java.io.File;
import java.util.Optional;

public class EchestTools implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }

        // Menu logic
        if (args.length == 1) {
            // menu logic
            return true;
        }

        // Commands requiring at least 2 arguments
        if (args.length >= 2) {
            String option = args[0].toLowerCase();
            String targetName = args[1];


            // Check if target is online
            OfflinePlayer targetOfflinePlayer = Bukkit.getOfflinePlayer(targetName);
            if (!targetOfflinePlayer.hasPlayedBefore() && !targetOfflinePlayer.isOnline()) {
                sender.sendMessage(ChatColor.RED + targetName + " has never played!");
                return true;
            }

            Player targetPlayer = targetOfflinePlayer.getPlayer();
            boolean isOnline = targetOfflinePlayer.isOnline();

            File playerDir = Bukkit.getWorlds().getFirst().getWorldFolder();
            File playerData;
            playerData = new File(playerDir, "playerdata" + File.separator + targetOfflinePlayer.getUniqueId() + ".dat");


            if(!playerData.exists()){
                sender.sendMessage(ChatColor.RED + targetName + "'s player data does not exist!");
                return true;
            }
            switch (option) {
                case "see":
                    if (!(sender instanceof Player viewer)) {
                        sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
                        return true;
                    }

                    // Create a copy of the ender chest to prevent modifications
                    Inventory enderChest = Bukkit.createInventory(null, InventoryType.ENDER_CHEST, ChatColor.BOLD + targetOfflinePlayer.getName() + "'s Ender Chest");
                    if(targetOfflinePlayer.getPlayer() != null){
                        enderChest.setContents(targetOfflinePlayer.getPlayer().getEnderChest().getContents());
                    } else{
                        try{
                            CompoundTag rootTag = NbtIo.readCompressed(playerData.toPath(), NbtAccounter.create(32768 * 1024));
                            Optional<ListTag> enderChestList = rootTag.getList("EnderItems");
                            if(!enderChestList.isPresent()){
                                sender.sendMessage(ChatColor.RED + targetName + "'s Ender Chest is empty!");
                                return true;
                            }
                            ListTag enderChestListTag = enderChestList.get();
                            for(int i = 0; i < enderChestListTag.size(); i++){
                                CompoundTag itemTag = enderChestListTag.getCompoundOrEmpty(i);
                                if(itemTag.contains("Slot")){
                                    Optional<Byte> slot = itemTag.getByte("Slot");
                                    Optional<Byte> count = itemTag.getByte("count");
                                    Optional<String> id = itemTag.getString("id");
                                    if(slot.isPresent() && count.isPresent() && id.isPresent()){
                                        Material material = Material.matchMaterial(id.get());
                                        ItemStack item = new ItemStack(material, count.get());
                                        enderChest.setItem(i, item);
                                    } else {
                                        sender.sendMessage(ChatColor.RED + "Couldn't parse the user's data");
                                        return true;
                                    }
                                }
                            }
                        } catch (Exception e){
                            sender.sendMessage(ChatColor.RED + playerData.getName() + "'s player data is not a valid file!");
                            return true;
                        }
                    }
                    Inventory copyInventory = Bukkit.createInventory(null, 27, ChatColor.BOLD + targetOfflinePlayer.getName() + "'s Ender Chest");
                    copyInventory.setContents(enderChest.getContents());

                    viewer.openInventory(copyInventory);
                    sender.sendMessage(ChatColor.GREEN + "Viewing " + targetOfflinePlayer.getName() + "'s ender chest!");
                    break;

                case "transfer":
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "Usage: /echesttools transfer <recipient> <giver>");
                        return true;
                    }

                    String giverName = args[2];
                    OfflinePlayer giverOfflinePlayer = Bukkit.getOfflinePlayer(giverName);


                    if(giverOfflinePlayer.getPlayer() != null && targetOfflinePlayer.getPlayer() != null){
                        ItemStack[] giverEnderChest = giverOfflinePlayer.getPlayer().getEnderChest().getContents();
                        targetOfflinePlayer.getPlayer().getEnderChest().setContents(giverEnderChest);
                    } else{
                        try{
                            File giverPlayerData;
                            giverPlayerData = new File(playerDir, "playerdata" + giverOfflinePlayer.getUniqueId() + ".dat");

                            CompoundTag giverTag = NbtIo.readCompressed(giverPlayerData.toPath(), NbtAccounter.create(32768 * 1024));
                            CompoundTag rootTag = NbtIo.readCompressed(playerData.toPath(), NbtAccounter.create(32768 * 1024));
                            Optional<ListTag> list =  giverTag.getList("EnderItems");
                            if(!list.isPresent()){
                                sender.sendMessage(ChatColor.RED + giverName + "'s Ender Chest is empty!");
                                return true;
                            }
                            ListTag listTag = list.get();
                            rootTag.put("EnderItems", listTag);
                            NbtIo.writeCompressed(rootTag, playerData.toPath());
                        } catch (Exception e){
                            sender.sendMessage(ChatColor.RED + playerData.getName() + "'s player data is not a valid file!");
                            return true;
                        }
                    }

                    sender.sendMessage(ChatColor.GREEN + "Successfully transferred " + giverOfflinePlayer.getName() +
                            "'s ender chest to " + targetOfflinePlayer.getName() + "'s!");
                    break;

                case "clear":
                    if(isOnline){
                        targetPlayer.getEnderChest().clear();
                    } else{
                        try{
                            CompoundTag rootTag = NbtIo.readCompressed(playerData.toPath(), NbtAccounter.create(32768 * 1024));
                            rootTag.put("EnderItems", new ListTag());
                            NbtIo.writeCompressed(rootTag, playerData.toPath());
                        } catch (Exception e){
                            sender.sendMessage(ChatColor.RED + playerData.getName() + "'s player data is not a valid file!");
                            return true;
                        }
                    }
                    sender.sendMessage(ChatColor.GREEN + "Successfully cleared " + targetOfflinePlayer.getName() + "'s ender chest!");
                    break;

                default:
                    sender.sendMessage(ChatColor.RED + "Unknown option: " + option);
                    sender.sendMessage(ChatColor.RED + "Valid options: see, transfer, clear");
                    return false;
            }
            return true;
        }

        return false;
    }
}