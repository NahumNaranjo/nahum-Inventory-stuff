package dev.nahum.nahumInventoryStuff;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import org.bukkit.Bukkit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

public class FileManager {
    final static NbtAccounter ACCOUNTER = NbtAccounter.create(32768 * 1024);

    static public File getWorldDir(){
        return Bukkit.getWorlds().getFirst().getWorldFolder();
    }
    static public File getPlayerFolder(){
        return new File(getWorldDir(), "playerdata");
    }
    static public File getDataFolder(){return NahumInventoryStuff.getInstance().getDataFolder();}
    static public NbtAccounter getAccounter(){return ACCOUNTER;}
    static public File getPlayerFile(UUID uuid) {
        return new File(getPlayerFolder(), uuid.toString() + ".dat");
    }

    static public File getSnapshotFolder(){
        File f = new File(getDataFolder(), "snapshots");
        return createFolder(getDataFolder().toString(), "snapshots");
    }

    static public File getBufferFolder(){
        return createFolder(getSnapshotFolder().toString(), "buffers");
    }

    static public File getPlayersSnapshotFolder(){
        return createFolder(getSnapshotFolder().toString(),  "players");
    }

    static public File getLonePlayerSnapshotFolder(Object player){
        return createFolder(getPlayersSnapshotFolder().toString(), DataParser.getUuidFromObject(player).toString());
    }
    static public File getPlayerDeathsFolder(Object player){
        return createFolder(getLonePlayerSnapshotFolder(player).toString(), "deaths");
    }
    static public File getPlayerJoinsFolder(Object player){
        return createFolder(getPlayersSnapshotFolder().toString(), "joins");
    }
    static public File getPlayerLeavesFolder(Object player){
        return createFolder(getPlayersSnapshotFolder().toString(), "leaves");
    }
    static public File getPlayerForcedSaves(Object player){
        return createFolder(getPlayersSnapshotFolder().toString(), "ForcedSaves");
    }

    static public File createFolder(String parent, String subfolder){
        File f = new File(parent, subfolder);
        if(!f.exists()){
            if(f.mkdirs()){
                return f;
            } else {
                GoodLogger.warn("Failed to create " + subfolder + " folder!");
                return null;
            }
        } else{
            return f;
        }
    }

    static public CompoundTag getPlayerData(UUID uuid){
        File file = getPlayerFile(uuid);
        try{
            return NbtIo.readCompressed(file.toPath(), ACCOUNTER);
        } catch (IOException e) {
            GoodLogger.error("Failed to read player data!\nError: " + e);
            return null;
        }
    }

    //IO helper methods
    static public boolean isFolderEmpty(File f){
        try(Stream<Path> stream = Files.list(f.toPath())){
            return stream.findAny().isEmpty();
        } catch (IOException e) {
            GoodLogger.warn("Failed to list player data!\nError: " + e);
            return true;
        }
    }

    //Backup system
    static public File getBackupFolder(){
        File backupFolder = new File(getDataFolder(), "backup");
        if(!backupFolder.exists()){
            try{
                backupFolder.mkdirs();
            } catch (SecurityException e){
                GoodLogger.error("Failed to create backup folder!\nError: " + e);
            }
        }
        return backupFolder;
    }


    static public File getLastBackup(){
        File backupFolder = getBackupFolder();

        if(backupFolder == null){
            GoodLogger.warn("Failed to find backup folder!");
            return null;
        }
        if(isFolderEmpty(backupFolder)){
            GoodLogger.warn("Failed to list player data in backup folder!");
            return null;
        }

        try (var stream = Files.list(backupFolder.toPath())) {
            Optional<Path> latestFile = stream
                    .filter(Files::isRegularFile) // Exclude directories
                    .max(Comparator.comparingLong(p -> p.toFile().lastModified()));

            if (latestFile.isPresent()) {
                return latestFile.get().toFile();
            } else {
                GoodLogger.warn("No files found in the directory.");
            }
        } catch (IOException e) {
            GoodLogger.warn("Failed to list player data in backup folder!\nError: " + e);
            e.printStackTrace();
        }
        return null;
    }

    static public boolean writeBackup(String name, Map<UUID, LinkedList<ListTag>> onlineUsers){
        File backupFolder = getBackupFolder();
        if(backupFolder == null){
            GoodLogger.error("Failed to find backup folder!");
            return false;
        }
        File newBackup;
        if(name == null){
            LocalDateTime now = LocalDateTime.now();
            newBackup =  new File(backupFolder.toPath() +
                    File.separator + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".nahumbackup");

            for(int i = 1; newBackup.exists(); i++){
                newBackup = new File(backupFolder.toPath() +
                        File.separator + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) +
                        "-(" + i + ")" + ".nahumbackup");
            }
        } else {
            newBackup = new File(backupFolder.toPath().toString(), name + ".nahumbackup");
        }

        Map<UUID, LinkedList<ListTag>> toWrite = fetchAllOfflineUserData(onlineUsers);

        CompoundTag rootTag = new CompoundTag();

        toWrite.forEach((uuid, listTag) -> {
            ListTag playerBackupGroup = new ListTag();
            for(ListTag playerListTag : listTag){
                playerBackupGroup.add(playerListTag);
            }

            rootTag.put(uuid.toString(), playerBackupGroup);
        });

        try{
            NbtIo.writeCompressed(rootTag, newBackup.toPath());
            return true;
        } catch (IOException e) {
            GoodLogger.error("Failed to write backup file!");
            e.printStackTrace();
            return false;
        }
    }

    static public boolean readBackup(String toRead) {
        File backupFolder = getBackupFolder();
        File playerFolder = getPlayerFolder();
        if (backupFolder == null || playerFolder == null) {
            GoodLogger.error("Failed to find required folders!");
            return false;
        }

        File backupFile = (toRead == null) ? getLastBackup() : new File(backupFolder, toRead);
        if (backupFile == null || !backupFile.exists()) {
            GoodLogger.error("Failed to find backup file!");
            return false;
        }

        try {
            CompoundTag rootTag = NbtIo.readCompressed(backupFile.toPath(), ACCOUNTER);

            for (String uuidString : rootTag.keySet()) {
                UUID uuid = UUID.fromString(uuidString);

                ListTag playerDataList = rootTag.getListOrEmpty(uuidString);

                if (playerDataList.size() >= 2) {
                    ListTag echestData = playerDataList.getListOrEmpty(0);
                    ListTag invData = playerDataList.getListOrEmpty(1);

                    OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

                    if (player.getPlayer() != null && player.getPlayer().isOnline()) {
                        var onlinePlayer = player.getPlayer();

                        onlinePlayer.getEnderChest().setContents(
                                Serializer.deserializeFromListTag(echestData, Serializer.ECHESTSIZE)
                        );

                        ItemStack[] fullInventory = Serializer.deserializeFromListTag(invData, Serializer.INVENTORYSIZE);
                        ItemStack[] mainInventory = new ItemStack[Serializer.MAIN_INVENTORY_SIZE];
                        System.arraycopy(fullInventory, 0, mainInventory, 0, Serializer.MAIN_INVENTORY_SIZE);

                        onlinePlayer.getInventory().setStorageContents(mainInventory);
                        onlinePlayer.getInventory().setArmorContents(new ItemStack[]{
                                fullInventory[Serializer.ARMOR_START],
                                fullInventory[Serializer.ARMOR_START + 1],
                                fullInventory[Serializer.ARMOR_START + 2],
                                fullInventory[Serializer.ARMOR_START + 3]
                        });
                        onlinePlayer.getInventory().setItemInOffHand(fullInventory[Serializer.OFFHAND_SLOT]);
                        continue;
                    }

                    File playerFile = getPlayerFile(uuid);
                    CompoundTag existingPlayerTag;

                    if (playerFile.exists()) {
                        existingPlayerTag = NbtIo.readCompressed(playerFile.toPath(), ACCOUNTER);
                    } else {
                        existingPlayerTag = new CompoundTag();
                    }

                    ItemStack[] fullInventory = Serializer.deserializeFromListTag(invData, Serializer.INVENTORYSIZE);
                    existingPlayerTag.put(NbtTags.getEchest(), echestData);
                    Serializer.applyFullInventoryToPlayer(existingPlayerTag, fullInventory);

                    NbtIo.writeCompressed(existingPlayerTag, playerFile.toPath());
                    GoodLogger.info("Restored data for " + uuid);
                    continue;
                }
                GoodLogger.warn("Failed to restore data due to invalid list format for " + uuid);
            }
            return true;
        } catch (IOException e) {
            GoodLogger.error("Failed to read backup file!\nError: " + e);
            return false;
        }
    }

    public static Map<UUID, LinkedList<ListTag>> fetchAllOnlineUserData() {
        Map<UUID, LinkedList<ListTag>> returning = new HashMap<>();
        GoodLogger.info("Fetching all user data...");
        int count = 0;

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            try {
                LinkedList<ListTag> data = new LinkedList<>();

                ItemStack[] echest = onlinePlayer.getEnderChest().getContents();

                ItemStack[] mainInv = onlinePlayer.getInventory().getStorageContents();
                ItemStack[] armor = onlinePlayer.getInventory().getArmorContents();
                ItemStack[] offHand = onlinePlayer.getInventory().getExtraContents();

                ItemStack[] fullInventory = new ItemStack[Serializer.INVENTORYSIZE];

                System.arraycopy(mainInv, 0, fullInventory, 0, mainInv.length);
                System.arraycopy(armor, 0, fullInventory, mainInv.length, armor.length);
                System.arraycopy(offHand, 0, fullInventory, mainInv.length + armor.length, offHand.length);

                ListTag echestListTag = Serializer.serializeToListTag(echest, onlinePlayer);
                ListTag invListTag = Serializer.serializeToListTag(fullInventory, onlinePlayer);

                data.add(echestListTag);
                data.add(invListTag);
                returning.put(onlinePlayer.getUniqueId(), data);

                GoodLogger.info("Fetched online player: " + onlinePlayer.getName());
                count++;
            } catch (Exception e) {
                GoodLogger.error("Failed to fetch data for online player " + onlinePlayer.getName() + ": " + e.getMessage());
            }
        }

        GoodLogger.success("Fetched all data for " + count + " online users ;D");
        return returning;
    }

    public static Map<UUID, LinkedList<ListTag>> fetchAllOfflineUserData(Map<UUID, LinkedList<ListTag>> onlineUserData) {
        Map<UUID, LinkedList<ListTag>> returning = new HashMap<>();
        GoodLogger.info("Fetching offline user data...");
        int count = 0;

        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            if (player.isOnline()) {
                continue;
            }

            UUID uuid = player.getUniqueId();
            CompoundTag tag = FileManager.getPlayerData(uuid);

            if (tag == null) {
                continue;
            }

            try {
                ListTag echestListTag = tag.getListOrEmpty(NbtTags.getEchest()).copy();
                ItemStack[] fullInventory = Serializer.buildFullInventoryFromPlayerTag(tag);
                boolean hasInventory = !echestListTag.isEmpty() || hasAnyItem(fullInventory);

                if (!hasInventory) {
                    continue;
                }

                ListTag invListTag = Serializer.serializeToListTag(fullInventory);

                LinkedList<ListTag> data = new LinkedList<>();
                data.add(echestListTag);
                data.add(invListTag);
                returning.put(uuid, data);

                count++;
            } catch (Exception e) {
                GoodLogger.error("Failed parsing offline data for UUID " + uuid + ": " + e.getMessage());
            }
        }

        GoodLogger.success("Fetched all data for " + count + " offline users ;D");
        returning.putAll(onlineUserData);
        return returning;
    }

    private static boolean hasAnyItem(ItemStack[] items) {
        for (ItemStack item : items) {
            if (item != null && !item.getType().isAir()) {
                return true;
            }
        }
        return false;
    }

    public static Map<UUID, ListTag> fetchOneUserData(String type){
        Map<UUID, ListTag> returning = new HashMap<>();
        if(!type.equals(NbtTags.getInventory()) && !type.equals(NbtTags.getEchest())){
            GoodLogger.error("Fetched user data for incorrect type: " + type);
            return returning;
        }
        int count = 0;
        GoodLogger.info("Fetching " + type + " user data...");
        for(OfflinePlayer player : Bukkit.getOfflinePlayers()){
            ListTag listTag;
            CompoundTag tag = FileManager.getPlayerData(player.getUniqueId());
            if(player.isOnline()){
                ItemStack[] contents;
                if(type.equals(NbtTags.getEchest())){
                    contents = player.getPlayer().getEnderChest().getContents();
                } else if(type.equals(NbtTags.getInventory())){
                    contents = player.getPlayer().getInventory().getContents();
                } else {
                    GoodLogger.warn("Failed to find player data for " + player.getName() + " due to datatype issues.");
                    continue;
                }

                if(contents == null){
                    GoodLogger.warn("Failed to find player data for " + player.getName() + "!");
                    continue;
                }

                listTag = Serializer.serializeToListTag(contents, player);
                returning.put(player.getUniqueId(), listTag);
                GoodLogger.info("Fetched " + player.getName());
                count++;
                continue;
            }

            if(tag == null){
                GoodLogger.warn(player.getName() + " has no player data!");
                continue;
            }

            listTag = tag.getListOrEmpty(type);
            if(listTag.isEmpty()){
                GoodLogger.warn(player.getName() + " has no " + type + " data!");
                continue;
            }
            returning.put(player.getUniqueId(), listTag);
            GoodLogger.info("Fetched " + player.getName());
            count++;
        }
        GoodLogger.info("Fetched " + type + " data for " + count + " users ;D");
        return returning;
    }

    public static void saveEchest(ItemStack[] contents, UUID uuid){
        File file = getPlayerFile(uuid);
        CompoundTag tag = getPlayerData(uuid);

        tag.put(NbtTags.getEchest(), Serializer.serializeToListTag(contents));
        try{
            NbtIo.writeCompressed(tag, file.toPath());
        } catch(IOException e){
            GoodLogger.error("Failed to save player data for " + Bukkit.getOfflinePlayer(uuid).getName() + ": \n" + e.getMessage());
        }
    }

    public static void saveInventory(ItemStack[] contents, Object recipient){
        UUID uuid = DataParser.getUuidFromObject(recipient);
        File file = getPlayerFile(uuid);
        CompoundTag tag = getPlayerData(uuid);

        if(tag == null){
            GoodLogger.warn("Failed to save player data for " + Bukkit.getOfflinePlayer(uuid).getName() + "!");
            return;
        }

        tag.put(NbtTags.getInventory(),Serializer.serializeToListTag(
                DataParser.getItemStackArray(contents, Serializer.MAIN_INVENTORY_SIZE, 0)));

        tag.put(NbtTags.getEquipment(),Serializer.serializeToListTag(
                DataParser.getItemStackArray(contents, Serializer.ARMORSIZE, Serializer.ARMOR_START)));

        tag.put(NbtTags.getOffhand(),Serializer.serializeToListTag(
                DataParser.getItemStackArray(contents, 1, Serializer.OFFHAND_SLOT)));

        try{
            NbtIo.writeCompressed(tag, file.toPath());
        } catch(IOException e){
            GoodLogger.error("Failed to save player data for " + Bukkit.getOfflinePlayer(uuid).getName() + ": \n" + e.getMessage());
        }
    }

    public static Player pasteInventory(ItemStack[] contents, Player recipient){
        recipient.getInventory().setContents(
                DataParser.getItemStackArray(contents, Serializer.MAIN_INVENTORY_SIZE, 0));

        recipient.getInventory().setArmorContents(
                DataParser.getItemStackArray(contents, Serializer.ARMORSIZE, Serializer.ARMOR_START));

        recipient.getInventory().setItemInOffHand(
                DataParser.getItemStackArray(contents, 1, Serializer.OFFHAND_SLOT)[0]);
        return recipient;
    }

    public static ItemStack[] loadInventory(Object giver){
        return Serializer.buildFullInventoryFromPlayerTag(getPlayerData(DataParser.getUuidFromObject(giver)));
    }


    public static boolean saveDeathSnapshot(){

        return true;
    }
    public static boolean saveJoinSnapshot(){
        return true;
    }
    public static boolean saveLeaveSnapshot(){
        return true;
    }
    public static boolean saveForcedSnapshot(){
        return true;
    }

}