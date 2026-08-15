package dev.nahum.nahumInventoryStuff;

import net.minecraft.nbt.ListTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.ChatColor;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

@SuppressWarnings({"deprecation", "SpellCheckingInspection", "NullableProblems"})

public class EchestTools implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 0) {
            return false;
        }

        // Menu logic
        if (args.length == 1) {
            return true;
        }

        // Commands requiring at least 2 arguments
        if (args.length >= 2) {
            String option = args[0].toLowerCase();
            String targetName = args[1];


            // Check if target exists
            OfflinePlayer targetPlayer = OfflinePlayerSync.getPlayer(targetName, sender);
            if (targetPlayer == null) {
                return true;
            }

            File playerData = FileManager.getPlayerFile(targetPlayer.getUniqueId());

            if (!playerData.exists()) {
                sender.sendMessage(ChatColor.RED + targetName + "'s player data does not exist!");
                return true;
            }
            switch (option) {
                case "see":
                    if (!sender.hasPermission("nahum.enderchesttools.see") && !sender.hasPermission("nahum.enderchesttools")) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                        return true;
                    }
                    Player viewer;
                    if (sender instanceof Player) {
                        viewer = (Player) sender;
                    } else {
                        sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
                        return true;
                    }
                    seeEchest(targetPlayer, viewer, sender);
                    sender.sendMessage(ChatColor.GREEN + "Viewing " + targetPlayer.getName() + "'s ender chest!");
                    GoodLogger.info(sender.getName() + " is looking at " +
                            targetPlayer.getName() + "'s echest using the command /enderchesttools with the argument \"see\"!");
                    String hello = "0";
                    break;

                case "transfer":
                    if (!sender.hasPermission("nahum.enderchesttools.transfer") && !sender.hasPermission("nahum.enderchesttools")) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                        return true;
                    }
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "Usage: /echesttools transfer <recipient> <giver>");
                        return true;
                    }

                    String giverName = args[2];
                    OfflinePlayer giverPlayer = OfflinePlayerSync.getPlayer(giverName, sender);
                    if (giverPlayer != null) {
                        transferEchest(targetPlayer, giverPlayer, sender, 1);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Couldn't get " + giverName + "'s OfflinePlayer!");
                        return true;
                    }
                    GoodLogger.info(sender.getName() + " transfered " +
                            targetPlayer.getName() + "'s echest to " + giverName + "'s using the command /enderchesttools with the argument \"transfer\"!");
                    break;
                case "swap":
                    if (!sender.hasPermission("nahum.enderchesttools.swap") && !sender.hasPermission("nahum.enderchesttools")) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                        return true;
                    }
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "Usage: /echesttools transfer <recipient> <giver>");
                        return true;
                    }


                    giverName = args[2];
                    giverPlayer = OfflinePlayerSync.getPlayer(giverName, sender);
                    if (giverPlayer != null) {
                        transferEchest(targetPlayer, giverPlayer, sender, 0);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Couldn't get " + giverName + "'s OfflinePlayer!");
                        return true;
                    }
                    GoodLogger.info(sender.getName() + " transfered " +
                            targetPlayer.getName() + "'s echest to " + giverName + "'s using the command /enderchesttools with the argument \"transfer\"!");
                    break;

                case "clear":
                    if (!sender.hasPermission("nahum.enderchesttools.clear") && !sender.hasPermission("nahum.enderchesttools")) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                        return true;
                    }
                    cleanEnderchest(targetPlayer, sender);
                    GoodLogger.info(sender.getName() + " has cleared " +
                            targetPlayer.getName() + "'s echest using the command /enderchesttools with the argument \"clear\"!");
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        String currentInput = null;
        if (args.length == 1) {
            if (sender.hasPermission("nahum.enderchesttools")) {
                completions.add("clear");
                completions.add("swap");
                completions.add("transfer");
                completions.add("see");
                completions.removeIf(option -> !option.toLowerCase().startsWith(args[0].toLowerCase()));
                return completions;
            }
            if (sender.hasPermission("nahum.enderchesttools.see")) {
                completions.add("see");
            }
            if (sender.hasPermission("nahum.enderchesttools.swap")) {
                completions.add("swap");
            }
            if (sender.hasPermission("nahum.enderchesttools.transfer")) {
                completions.add("transfer");
            }
            if (sender.hasPermission("nahum.enderchesttools.clear")) {
                completions.add("clear");
            }
            currentInput = args[0].toLowerCase();
        }

        if (args.length == 2) {
            List<String> players = new LinkedList<>();
            for (OfflinePlayer player : OfflinePlayerSync.getAllPlayers()) {
                players.add(player.getName());
            }
            Collections.sort(players);
            players.removeIf(option -> !option.toLowerCase().startsWith(args[1]));
            return players;
        }

        if (currentInput == null) {
            return new ArrayList<>();
        }
        String unmatched = currentInput;
        completions.removeIf(option -> !option.toLowerCase().startsWith(unmatched));
        return completions;
    }

    public static boolean cleanEnderchest(OfflinePlayer player, CommandSender sender) {
        if (player.getPlayer() != null) {
            player.getPlayer().getEnderChest().clear();
        } else {
            try {
                CompoundTag rootTag = FileManager.getPlayerData(player.getUniqueId());
                File playerData = FileManager.getPlayerFile(player.getUniqueId());

                if (rootTag == null) {
                    sender.sendMessage(ChatColor.RED + player.getName() + "'s player data is not a valid file!");
                    return false;
                }
                rootTag.put(NbtTags.getEchest(), new ListTag());
                NbtIo.writeCompressed(rootTag, playerData.toPath());
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + player.getName() + "'s player data is not a valid file!");
                return false;
            }
        }
        sender.sendMessage(ChatColor.GREEN + "Successfully cleared " + player.getName() + "'s ender chest!");
        return true;
    }

    public static boolean cleanEnderchestNoSend(OfflinePlayer player) {
        if (player.getPlayer() != null) {
            player.getPlayer().getEnderChest().clear();
        } else {
            try {
                CompoundTag rootTag = FileManager.getPlayerData(player.getUniqueId());
                File playerData = FileManager.getPlayerFile(player.getUniqueId());

                if (rootTag == null) {
                    return false;
                }
                rootTag.put(NbtTags.getEchest(), new ListTag());
                NbtIo.writeCompressed(rootTag, playerData.toPath());
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public static boolean transferEchest(OfflinePlayer recipient, OfflinePlayer giverPlayer, CommandSender sender, int mode) {
        if (giverPlayer == null) {
            if (sender != null) {
                sender.sendMessage(ChatColor.RED + "Couldn't get the giver player's data!");
            }
            GoodLogger.warn("Attempted to transfer an ender chest with a null giver player.");
            return false;
        }

        String giverName = giverPlayer.getName();

        if (OfflinePlayerSync.isOnline(giverPlayer) && OfflinePlayerSync.isOnline(recipient)) {
            if (mode == 0) {
                ItemStack[] helper = recipient.getPlayer().getEnderChest().getContents();
                recipient.getPlayer().getEnderChest().setContents(giverPlayer.getPlayer().getEnderChest().getContents());
                giverPlayer.getPlayer().getEnderChest().setContents(helper);
            } else {
                recipient.getPlayer().getEnderChest().setContents(giverPlayer.getPlayer().getEnderChest().getContents());
            }
        } else if (OfflinePlayerSync.isOnline(giverPlayer) || OfflinePlayerSync.isOnline(recipient)) {
            Player onlinePlayer;
            if (OfflinePlayerSync.isOnline(recipient)) {
                onlinePlayer = recipient.getPlayer();
                CompoundTag rootTag = FileManager.getPlayerData(giverPlayer.getUniqueId());
                if (rootTag == null) {
                    GoodLogger.warn("Attempted to transfer an ender chest with a null player.");
                    return false;
                }

                ListTag giverEchest = rootTag.getListOrEmpty(NbtTags.getEchest());
                if (mode == 0) {
                    ListTag recipientEchestTag = Serializer.serializeToListTag(onlinePlayer.getEnderChest().getContents());

                    rootTag.put(NbtTags.getEchest(), recipientEchestTag);
                    try{
                        NbtIo.writeCompressed(rootTag, FileManager.getPlayerFile(giverPlayer.getUniqueId()).toPath());
                    } catch (Exception e){
                        GoodLogger.warn("Could not save the giver player's data!");
                        sender.sendMessage(ChatColor.RED + "Could not save the giver player's data!");
                        return false;
                    }
                }
                ItemStack[] giverItemStack = Serializer.deserializeFromListTag(giverEchest, Serializer.ECHESTSIZE);
                onlinePlayer.getEnderChest().setContents(giverItemStack);
            } else {
                onlinePlayer = giverPlayer.getPlayer();

            }
        } else if (!OfflinePlayerSync.isOnline(recipient) && !OfflinePlayerSync.isOnline(giverPlayer)) {
            try {
                File recipientData = FileManager.getPlayerFile(recipient.getUniqueId());
                CompoundTag giverTag = FileManager.getPlayerData(giverPlayer.getUniqueId());
                CompoundTag rootTag = FileManager.getPlayerData(recipient.getUniqueId());

                if (rootTag == null || giverTag == null) {
                    GoodLogger.warn((rootTag == null ? recipient.getName() : giverName) + "'s player data is not a valid file!");
                    sender.sendMessage(ChatColor.RED + (rootTag == null ? recipient.getName() : giverName) + "'s player data is not a valid file!");
                    return false;
                }

                Optional<ListTag> list = giverTag.getList(NbtTags.getEchest());
                if (list.isEmpty()) {
                    GoodLogger.warn(giverName + "'s Ender Chest is empty!");
                    sender.sendMessage(ChatColor.RED + giverName + "'s Ender Chest is empty!");
                    return false;
                }

                ListTag listTag = list.get();
                rootTag.put(NbtTags.getEchest(), listTag);
                NbtIo.writeCompressed(rootTag, recipientData.toPath());
            } catch (Exception e) {
                GoodLogger.error(recipient.getName() + "'s player data is not a valid file!", e);
                sender.sendMessage(ChatColor.RED + recipient.getName() + "'s player data is not a valid file!");
                return false;
            }
        }


        sender.sendMessage(ChatColor.GREEN + "Successfully transferred " + giverPlayer.getName() +
                "'s ender chest to " + recipient.getName() + "'s!");
        return true;
    }

    public static boolean seeEchest(OfflinePlayer targetPlayer, Player viewer, CommandSender sender){
        // Create a copy of the ender chest to prevent modifications
        Inventory enderChest = Bukkit.createInventory(null, InventoryType.ENDER_CHEST, ChatColor.BOLD + targetPlayer.getName() + "'s Ender Chest");
        if(targetPlayer.getPlayer() != null){
            enderChest.setContents(targetPlayer.getPlayer().getEnderChest().getContents());
            viewer.openInventory(enderChest);
        } else{
            try{
                CompoundTag rootTag = FileManager.getPlayerData(targetPlayer.getUniqueId());
                if(rootTag == null){
                    sender.sendMessage(ChatColor.RED + targetPlayer.getName() + "'s data file is empty!");
                    return true;
                }

                Optional<ListTag> enderChestList = rootTag.getList(NbtTags.getEchest());
                if(enderChestList.isEmpty()){
                    sender.sendMessage(ChatColor.RED + targetPlayer.getName() + "'s Ender Chest is empty!");
                    return true;
                }

                ListTag enderChestListTag = enderChestList.get();
                for(int i = 0; i < enderChestListTag.size(); i++){
                    CompoundTag itemTag = enderChestListTag.getCompoundOrEmpty(i);
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
                            enderChest.setItem(slot.get(), item);
                        } else {
                            sender.sendMessage(ChatColor.RED + "Couldn't parse the user's data");
                            return true;
                        }
                    }
                }
                viewer.openInventory(enderChest);
            } catch (Exception e){
                sender.sendMessage(ChatColor.RED + targetPlayer.getName() + "'s player data is not a valid file!");
            }
        }
        viewer.openInventory(enderChest);
        return true;
    }
}