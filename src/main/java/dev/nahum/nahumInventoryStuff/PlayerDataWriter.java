package dev.nahum.nahumInventoryStuff;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class PlayerDataWriter {
    public PlayerDataReader reader;

    PlayerDataWriter(PlayerDataReader reader) {
        this.reader = Objects.requireNonNull(reader, this.getClass().getName() + ": reader cannot be null");
    }

    public void loadAdminSnapshot(Object admin, Object player) {
        loadAdminSnapshot(PathManager.getPlayerBuffer(admin, player), player);
    }

    public void loadAdminSnapshot(File f, Object player) {
        GoodLogger.debug("started loadAdminSnapshot");
        UUID playerUuid = DataParser.getUuidFromObject(player);
        File snapshot = null;
        if (playerUuid == null) {
            GoodLogger.debug("Player is null!");
            return;
        }
        GoodLogger.debug("Player UUID: " + playerUuid);
        GoodLogger.debug("folder/file: " + f.getAbsolutePath());
        if (f.isDirectory()) {
            try (var stream = Files.list(f.toPath())) {
                Optional<Path> newestFile = stream
                        .filter(Files::isRegularFile)
                        .max(Comparator.comparingLong(p -> p.toFile().lastModified()));

                if (newestFile.isPresent()) {
                    snapshot = newestFile.get().toFile();
                } else {
                    GoodLogger.debug("No file found!");
                    return;
                }
            } catch (IOException e) {
                GoodLogger.debug("Error found: " + e.getMessage());
                e.printStackTrace();
            }
            GoodLogger.debug("got newest file");
        } else {
            snapshot = f;
        }
        if (snapshot != null) {
            GoodLogger.debug("snapshot wasn't null");
            CompoundTag snapshotTag = reader.getPlayerSnapshotData(snapshot);
            ListTag inv = snapshotTag.getListOrEmpty(NbtTags.getInventory());
            ListTag echest = snapshotTag.getListOrEmpty(NbtTags.getEchest());
            CompoundTag additionalInfo = snapshotTag.getCompoundOrEmpty("additionalInfo");
            OfflinePlayer offlinePlayer = ((OfflinePlayer) player);
            if (offlinePlayer == null) {
                return;
            }
            GoodLogger.debug("OfflinePlayer " + offlinePlayer.getName());
            Player online = null;
            if (offlinePlayer.isOnline()) {
                online = offlinePlayer.getPlayer();
                GoodLogger.debug("Player was online");
            }
            GoodLogger.debug("player wasnt online");
            if (!inv.isEmpty() && online != null)
                pasteInventory(Serializer.buildFullInventoryFromPlayerTag(snapshotTag), online);
            if (!echest.isEmpty() && online != null)
                pasteEchest(Serializer.deserializeFromListTag(echest, Serializer.ECHESTSIZE), online);
            if (!inv.isEmpty())
                saveInventory(Serializer.buildFullInventoryFromPlayerTag(snapshotTag), offlinePlayer);
            if (!echest.isEmpty())
                saveEchest(Serializer.deserializeFromListTag(echest, Serializer.ECHESTSIZE), offlinePlayer.getUniqueId());
            if (!additionalInfo.isEmpty()) {
                String linkedTo = additionalInfo.getStringOr("linkedTo", "null");
                if (linkedTo.equals("null")) {
                    GoodLogger.debug("linkedTo is null");
                    return;
                }
                Path path = Paths.get(linkedTo);
                if (!path.toFile().exists()) {
                    return;
                }
                CompoundTag linkedToTag = reader.getPlayerSnapshotData(path.toFile());
                GoodLogger.debug("linkedToPath: " + path);
                GoodLogger.debug("linkedToTag: " + linkedToTag);
                ListTag inv2 = linkedToTag.getListOrEmpty(NbtTags.getInventory());
                ListTag echest2 = linkedToTag.getListOrEmpty(NbtTags.getEchest());
                String uuidString = additionalInfo.getStringOr("uuid", "null");
                if (uuidString.equals("null")) {
                    return;
                }
                GoodLogger.debug("uuid: " + uuidString);
                offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuidString));
                online = null;
                if (offlinePlayer.isOnline()) {
                    online = offlinePlayer.getPlayer();
                    GoodLogger.debug("Player: " + online.getName());
                }
                GoodLogger.debug("Player: " + offlinePlayer.getName());
                if (!inv2.isEmpty() && online != null)
                    pasteInventory(Serializer.buildFullInventoryFromPlayerTag(linkedToTag), online);
                if (!echest2.isEmpty() && online != null)
                    pasteEchest(Serializer.deserializeFromListTag(echest2, Serializer.ECHESTSIZE), online);
                if (!inv2.isEmpty())
                    saveInventory(Serializer.buildFullInventoryFromPlayerTag(linkedToTag), offlinePlayer);
                if (!echest2.isEmpty())
                    saveEchest(Serializer.deserializeFromListTag(echest2, Serializer.ECHESTSIZE), offlinePlayer.getUniqueId());
                GoodLogger.debug("succeeded to restore both of their inventories");
            }
        }
    }

    public void saveEchest(ListTag contents, Object uuid){
        saveEchest(Serializer.deserializeFromListTag(contents, Serializer.ECHESTSIZE), DataParser.getUuidFromObject(uuid));
    }

    public void saveEchest(ItemStack[] contents, UUID uuid) {
        File file = PathManager.getPlayerFile(uuid);
        CompoundTag tag = reader.getPlayerData(uuid);

        tag.put(NbtTags.getEchest(), Serializer.serializeToListTag(contents));
        try {
            NbtIo.writeCompressed(tag, file.toPath());
        } catch (IOException e) {
            GoodLogger.error("Failed to save player data for " + Bukkit.getOfflinePlayer(uuid).getName() + ": \n" + e.getMessage());
        }
    }

    public void saveInventory(CompoundTag contents, Object uuid){
        saveInventory(Serializer.buildFullInventoryFromPlayerTag(contents), DataParser.getUuidFromObject(uuid));
    }

    public void saveInventory(ListTag contents, Object uuid){
        saveInventory(Serializer.deserializeFromListTag(contents, Serializer.MAIN_INVENTORY_SIZE), DataParser.getUuidFromObject(uuid));
    }

    public void saveInventory(ItemStack[] contents, Object recipient) {
        UUID uuid = DataParser.getUuidFromObject(recipient);
        File file = PathManager.getPlayerFile(uuid);
        CompoundTag tag = reader.getPlayerData(uuid);

        if (tag == null) {
            GoodLogger.warn("Failed to save player data for " + Bukkit.getOfflinePlayer(uuid).getName() + "!");
            return;
        }

        if (contents != null) {
            tag.put(NbtTags.getInventory(), Serializer.serializeToListTag(
                    DataParser.getItemStackArray(contents, Serializer.MAIN_INVENTORY_SIZE, 0)));

            tag.put(NbtTags.getEquipment(), Serializer.serializeArmorToCompoundTag(
                    DataParser.getItemStackArray(contents, Serializer.ARMORSIZE + 1, Serializer.ARMOR_START)));
        }

        try {
            NbtIo.writeCompressed(tag, file.toPath());
        } catch (IOException e) {
            GoodLogger.error("Failed to save player data for " + Bukkit.getOfflinePlayer(uuid).getName() + ": \n" + e.getMessage());
        }
    }

    public void saveSnapshot(ListTag inventory, ListTag echest, Object uuid, File file){
        saveSnapshot(
                Serializer.deserializeFromListTag(inventory, Serializer.MAIN_INVENTORY_SIZE),
                Serializer.deserializeFromListTag(echest, Serializer.ECHESTSIZE),
                DataParser.getUuidFromObject(uuid),
                file
        );
    }

    public void saveSnapshot(ItemStack[] inventory, ItemStack[] echest, UUID uuid, File file) {
        CompoundTag tag = new CompoundTag();

        if (inventory != null) {
            tag.put(NbtTags.getInventory(), Serializer.serializeToListTag(
                    DataParser.getItemStackArray(inventory, Serializer.MAIN_INVENTORY_SIZE, 0)));

            tag.put(NbtTags.getEquipment(), Serializer.serializeToListTag(
                    DataParser.getItemStackArray(inventory, Serializer.ARMORSIZE, Serializer.ARMOR_START)));

            tag.put(NbtTags.getOffhand(), Serializer.serializeToListTag(
                    DataParser.getItemStackArray(inventory, 1, Serializer.OFFHAND_SLOT)));
        }

        if (echest != null) {
            tag.put(NbtTags.getEchest(), Serializer.serializeToListTag(echest));
        }

        try {
            NbtIo.writeCompressed(tag, file.toPath());
        } catch (IOException e) {
            GoodLogger.error("Failed to save player data for " + Bukkit.getOfflinePlayer(uuid).getName() + ": \n" + e.getMessage());
        }
    }

    public boolean writeStringToSnapshot(File snapshot, UUID uuid, String message, String key) {
        CompoundTag tag = reader.getPlayerSnapshotData(snapshot);
        CompoundTag stringTag = tag.getCompoundOrEmpty("additionalInfo");
        if (stringTag.isEmpty()) {
            stringTag = new CompoundTag();
        }
        stringTag.putString(key, message);

        tag.put("additionalInfo", stringTag);

        try {
            NbtIo.writeCompressed(tag, snapshot.toPath());
            return true;
        } catch (IOException e) {
            GoodLogger.error("Failed to save player data for " + uuid.toString() + ": \n" + e.getMessage());
            return false;
        }
    }

    public void pasteInventory(CompoundTag contents, Player recipient) {
        pasteInventory(Serializer.buildFullInventoryFromPlayerTag(contents), recipient);
    }

    public void pasteInventory(ListTag contents, Player recipient) {
        pasteInventory(Serializer.deserializeFromListTag(contents, Serializer.MAIN_INVENTORY_SIZE), recipient);
    }

    public void pasteInventory(ItemStack[] contents, Player recipient) {
        recipient.getInventory().setContents(
                DataParser.getItemStackArray(contents, Serializer.MAIN_INVENTORY_SIZE, 0));

        recipient.getInventory().setArmorContents(
                DataParser.getItemStackArray(contents, Serializer.ARMORSIZE, Serializer.ARMOR_START));

        recipient.getInventory().setItemInOffHand(
                DataParser.getItemStackArray(contents, 1, Serializer.OFFHAND_SLOT)[0]);
        recipient.updateInventory();
    }

    public void pasteEchest(ListTag contents, Player recipient) {
        pasteEchest(Serializer.deserializeFromListTag(contents, Serializer.ECHESTSIZE), recipient);
    }

    public void pasteEchest(ItemStack[] contents, Player recipient) {
        recipient.getInventory().setContents(contents);
        recipient.updateInventory();
    }

    public ItemStack[] loadInventory(Object giver) {
        return Serializer.buildFullInventoryFromPlayerTag(reader.getPlayerData(DataParser.getUuidFromObject(giver)));
    }
}
