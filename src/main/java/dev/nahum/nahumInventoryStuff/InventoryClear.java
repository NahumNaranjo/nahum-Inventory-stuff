package dev.nahum.nahumInventoryStuff;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class InventoryClear implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        SenderLogger toSender = new SenderLogger(sender);
        InvTools invTools = new InvTools(sender);
        EchestTools echestTools = new EchestTools(sender);
        if (args.length < 2) {
            toSender.warning("Usage: /inventoryclean <option> <username>");
            toSender.warning("Options: all, echest, inventory");
            return true;
        }

        String option = args[0].toLowerCase();
        String playerName = args[1];

        // Check if player exists
        OfflinePlayer targetPlayer = OfflinePlayerSync.getPlayer(playerName);
        if (targetPlayer == null) {
            return true;
        }

        String playerDisplayName = targetPlayer.getName();

        switch (args[0].toLowerCase()) {
            case "all":
                if (!sender.hasPermission("nahum.inventoryclear") &&
                        (!sender.hasPermission("nahum.inventorytools") && !sender.hasPermission("nahum.echesttools")) &&
                        (!sender.hasPermission("nahum.echesttools.clear") && !sender.hasPermission("nahum.inventorytools.clear"))) {
                    toSender.error("You do not have permission to use this command!");
                    return true;
                }
                invTools.cleanInventory(targetPlayer);
                echestTools.cleanEnderchest(targetPlayer);
                toSender.success("Successfully cleared all storage for " + playerDisplayName + "!");
                GoodLogger.info(sender.getName() + " has cleared all of " +
                        playerDisplayName + "'s inventories using the command /inventoryclear!");
                break;

            case "echest":
                if (!sender.hasPermission("nahum.inventoryclear") &&
                        !sender.hasPermission("nahum.echesttools") &&
                        !sender.hasPermission("nahum.echesttools.clear")) {
                    toSender.error("You do not have permission to use this command!");
                    return true;
                }
                echestTools.cleanEnderchest(targetPlayer);
                GoodLogger.info(sender.getName() + " has cleared " +
                        playerDisplayName + "'s enderchest using the command /inventoryclear!");
                break;

            case "inventory":
                if (!sender.hasPermission("nahum.inventoryclear") &&
                        !sender.hasPermission("nahum.inventorytools") &&
                        !sender.hasPermission("nahum.inventorytools.clear")) {
                    toSender.error("You do not have permission to use this command!");
                    return true;
                }
                invTools.cleanInventory(targetPlayer);
                GoodLogger.info(sender.getName() + " has cleared " +
                        playerDisplayName + "'s inventory using the command /inventoryclear!");
                break;

            default:
                toSender.warning("Unknown option: " + option);
                toSender.warning("Valid options: all, echest, inventory");
                return false;
        }

        return true;
    }
}