package dev.nahum.nahumInventoryStuff;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Objects;

public class EchestTools {
    private final PlayerDataReader reader;
    private final PlayerDataWriter writer;
    private final SnapshotManager snapshotManager;
    private final SenderLogger toSender;

    public EchestTools(PlayerDataReader reader, PlayerDataWriter writer, CommandSender sender) {
        this.reader = Objects.requireNonNull(reader);
        this.writer = Objects.requireNonNull(writer);
        this.snapshotManager = new SnapshotManager(reader, writer);
        this.toSender = new SenderLogger(sender);
    }
    public EchestTools(CommandSender sender) {
        this.reader = new PlayerDataReader();
        this.writer = new PlayerDataWriter(this.reader);
        this.snapshotManager = new SnapshotManager(this.reader, this.writer);
        this.toSender = new SenderLogger(sender);
    }

    public void saveEditData(OfflinePlayer viewer, OfflinePlayer targetPlayer, Inventory echest) {
        snapshotManager.performAdminBufferSave(
                viewer,
                targetPlayer,
                null,
                echest.getContents(),
                viewer.getName() + " edited " + targetPlayer.getName() + "'s inventory!",
                null,
                null
        );
    }

    public boolean cleanEnderchest(OfflinePlayer player) {

        if (player.getPlayer() != null) {
            player.getPlayer().getEnderChest().clear();
        } else {
            try {
                CompoundTag rootTag = reader.getPlayerData(player.getUniqueId());
                File playerData = PathManager.getPlayerFile(player.getUniqueId());

                if (rootTag == null) {
                    toSender.error(player.getName() + "'s player data is not a valid file!");
                    return false;
                }

                rootTag.remove(NbtTags.getEchest());
                NbtIo.writeCompressed(rootTag, playerData.toPath());
            } catch (Exception e) {
                toSender.error(player.getName() + "'s player data is not a valid file!");
                return false;
            }
        }
        toSender.success("Successfully cleared " + player.getName() + "'s ender chest!");
        return true;
    }

    public boolean transferEchest(OfflinePlayer recipient, OfflinePlayer giverPlayer, int mode) {
        if (giverPlayer == null) {
            toSender.error("Couldn't get the giver player's data!");
            GoodLogger.warn("Attempted to transfer an ender chest with a null giver player.");
            return false;
        }

        String giverName = giverPlayer.getName();

        Player giverOnline = OfflinePlayerSync.getOnlinePlayer(giverPlayer);
        Player recipientOnline = OfflinePlayerSync.getOnlinePlayer(recipient);
        if (recipientOnline != null && giverOnline != null) {
            if (mode == 0) {
                ItemStack[] helper = recipientOnline.getEnderChest().getContents();
                recipientOnline.getEnderChest().setContents(giverOnline.getEnderChest().getContents());
                giverOnline.getEnderChest().setContents(helper);
            } else {
                recipientOnline.getEnderChest().setContents(giverOnline.getEnderChest().getContents());
            }
        } else {
            try {
                CompoundTag giverTag = reader.getPlayerData(giverPlayer.getUniqueId());
                CompoundTag recipientTag = reader.getPlayerData(recipient.getUniqueId());

                if (recipientTag == null || giverTag == null) {
                    GoodLogger.warn((recipientTag == null ? recipient.getName() : giverName) + "'s player data is not a valid file!");
                    toSender.error((recipientTag == null ? recipient.getName() : giverName) + "'s player data is not a valid file!");
                    return false;
                }

                ItemStack[] giverEchest = Serializer.deserializeFromListTag(giverTag.getListOrEmpty(NbtTags.getEchest()), Serializer.ECHESTSIZE);
                ItemStack[] recipientEchest = Serializer.deserializeFromListTag(recipientTag.getListOrEmpty(NbtTags.getEchest()), Serializer.ECHESTSIZE);

                if (giverOnline != null) {
                    giverEchest = giverOnline.getEnderChest().getContents();
                }
                if (recipientOnline != null) {
                    recipientEchest = recipientOnline.getEnderChest().getContents();
                }

                if (mode == 0) {
                    writer.saveEchest(giverEchest, DataParser.getUuidFromObject(giverPlayer));
                    if (giverOnline != null) giverOnline.updateInventory();
                }
                writer.saveEchest(recipientEchest, DataParser.getUuidFromObject(recipient));
                if (recipientOnline != null) recipientOnline.updateInventory();

            } catch (Exception e) {
                GoodLogger.error(recipient.getName() + "'s player data is not a valid file!", e);
                toSender.error(recipient.getName() + "'s player data is not a valid file!");
                return false;
            }
        }


        if (mode == 0) {
            toSender.success("Successfully swapped " + giverName + "'s echest with " + recipient.getName() + "'s!");
            return true;
        }

        toSender.success("Successfully transferred " + giverPlayer.getName() +
                "'s ender chest to " + recipient.getName() + "'s!"
        );
        return true;
    }

    public boolean seeEchest(OfflinePlayer targetPlayer, Player viewer) {
        Inventory enderChest = Bukkit.createInventory(null, InventoryType.ENDER_CHEST, ChatColor.BLACK + targetPlayer.getName() + "'s Ender Chest");
        if (targetPlayer.getPlayer() != null) {
            enderChest.setContents(targetPlayer.getPlayer().getEnderChest().getContents());
            viewer.openInventory(enderChest);
        } else {
            try {
                CompoundTag rootTag = reader.getPlayerData(targetPlayer.getUniqueId());
                if (rootTag == null) {
                    toSender.error(targetPlayer.getName() + "'s data file is empty!");
                    return true;
                }

                enderChest.setContents(Serializer.deserializeFromListTag(rootTag.getListOrEmpty(NbtTags.getEchest()), Serializer.ECHESTSIZE));
                viewer.openInventory(enderChest);

            } catch (Exception e) {
                toSender.error(targetPlayer.getName() + "'s player data is not a valid file!");
            }
        }
        return true;
    }

    public boolean editEchest(OfflinePlayer targetPlayer, Player viewer) {
        Inventory enderChest = Bukkit.createInventory(null, InventoryType.ENDER_CHEST, ChatColor.BOLD + targetPlayer.getName() + "'s Ender Chest");
        if (targetPlayer.getPlayer() != null) {
            enderChest.setContents(targetPlayer.getPlayer().getEnderChest().getContents());
            saveEditData(viewer, targetPlayer, enderChest);
            viewer.openInventory(targetPlayer.getPlayer().getEnderChest());
        } else {
            try {
                CompoundTag rootTag = reader.getPlayerData(targetPlayer.getUniqueId());
                if (rootTag == null) {
                    toSender.error(targetPlayer.getName() + "'s data file is empty!");
                    return true;
                }

                enderChest.setContents(Serializer.deserializeFromListTag(rootTag.getListOrEmpty(NbtTags.getEchest()), Serializer.ECHESTSIZE));
                saveEditData(viewer, targetPlayer, enderChest);
                viewer.openInventory(enderChest);
            } catch (Exception e) {
                toSender.error(targetPlayer.getName() + "'s player data is not a valid file! ");
                GoodLogger.error(targetPlayer.getName() + "'s player data is not a valid file! " + e.getMessage() + "\n" + e.getCause());
            }
        }
        return true;
    }
}
