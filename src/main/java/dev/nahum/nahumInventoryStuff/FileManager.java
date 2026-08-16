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
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    static public File getAdminFolder(Object player){
        if(player instanceof String){
            return createFolder(getBufferFolder().toString(), "console");
        } else if(player == null){
            return createFolder(getBufferFolder().toString(), "console");
        } else {
            return createFolder(getBufferFolder().toString(), DataParser.getUuidFromObject(player).toString());
        }

    }
    static public File getPlayerBuffer(Object admin, Object player) { return createFolder(getAdminFolder(admin).toString(), DataParser.getUuidFromObject(player).toString()); }

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
    static public File getPlayerForcedFolder(Object player){
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

    private static boolean deleteFolder(File directory) {
        File[] contents = directory.listFiles();
        if (contents != null) {
            for (File file : contents) {
                deleteFolder(file);
            }
        }
        return directory.delete();
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

    static public CompoundTag getPlayerSnapshotData(File file){
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

        if(contents != null){
            tag.put(NbtTags.getInventory(),Serializer.serializeToListTag(
                    DataParser.getItemStackArray(contents, Serializer.MAIN_INVENTORY_SIZE, 0)));

            tag.put(NbtTags.getEquipment(),Serializer.serializeToListTag(
                    DataParser.getItemStackArray(contents, Serializer.ARMORSIZE, Serializer.ARMOR_START)));

            tag.put(NbtTags.getOffhand(),Serializer.serializeToListTag(
                    DataParser.getItemStackArray(contents, 1, Serializer.OFFHAND_SLOT)));
        }

        try{
            NbtIo.writeCompressed(tag, file.toPath());
        } catch(IOException e){
            GoodLogger.error("Failed to save player data for " + Bukkit.getOfflinePlayer(uuid).getName() + ": \n" + e.getMessage());
        }
    }

    public static void saveSnapshot(ItemStack[] inventory, ItemStack[] echest,UUID uuid, File file){
        CompoundTag tag = getPlayerSnapshotData(file);

        if(tag == null){
            GoodLogger.warn("Failed to save player data for " + Bukkit.getOfflinePlayer(uuid).getName() + "!");
            return;
        }

        if(inventory != null){
            tag.put(NbtTags.getInventory(),Serializer.serializeToListTag(
                    DataParser.getItemStackArray(inventory, Serializer.MAIN_INVENTORY_SIZE, 0)));

            tag.put(NbtTags.getEquipment(),Serializer.serializeToListTag(
                    DataParser.getItemStackArray(inventory, Serializer.ARMORSIZE, Serializer.ARMOR_START)));

            tag.put(NbtTags.getOffhand(),Serializer.serializeToListTag(
                    DataParser.getItemStackArray(inventory, 1, Serializer.OFFHAND_SLOT)));
        }

        if(echest != null){
            tag.put(NbtTags.getEchest(), Serializer.serializeToListTag(echest));
        }

        try{
            NbtIo.writeCompressed(tag, file.toPath());
        } catch(IOException e){
            GoodLogger.error("Failed to save player data for " + Bukkit.getOfflinePlayer(uuid).getName() + ": \n" + e.getMessage());
        }
    }
    public static boolean writeString(File snapshot, UUID uuid, String message, String key){
        CompoundTag tag = getPlayerSnapshotData(snapshot);
        ListTag listTag = new ListTag();
        listTag.add(Serializer.serializeString(message, key));

        try{
            NbtIo.writeCompressed(tag, snapshot.toPath());
            return true;
        } catch(IOException e){
            GoodLogger.error("Failed to save player data for " + uuid.toString() + ": \n" + e.getMessage());
            return false;
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

    public static LocalDate getAgeFromName(String fileName) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        LocalDateTime dateTime = LocalDateTime.parse(fileName, formatter);
        return dateTime.toLocalDate();
    }
    public static LocalDateTime getDateTimeFromName(String fileName) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        LocalDateTime dateTime = LocalDateTime.parse(fileName, formatter);
        return dateTime;
    }

    public static void deleteOldSnapshots(File folder, String name) {
        Integer max = (Integer) ConfigManager.getConfig(name);
        if (max == null) max = 10;

        List<File> files = new ArrayList<>();
        try (Stream<Path> stream = Files.list(folder.toPath())) {
            stream.filter(Files::isRegularFile)
                    .forEach(path -> files.add(path.toFile()));
        } catch (IOException e) {
            System.err.println("Error reading directory: " + e.getMessage());
            return;
        }

        if (max == 0 || files.size() <= max) return; // Nothing to delete

        Map<LocalDateTime, File> map = new HashMap<>();
        List<LocalDateTime> dates = new ArrayList<>();

        for (File file : files) {
            LocalDateTime date = getDateTimeFromName(file.getName());
            if (date != null) {
                map.put(date, file);
                dates.add(date);
            }
        }

        dates.sort(Comparator.naturalOrder());
        int toDelete = files.size() - max;
        int deleted = 0;

        for (LocalDateTime date : dates) {
            if (deleted >= toDelete) break;

            File file = map.get(date);
            if (file != null && file.delete()) {
                GoodLogger.debug("Deleted old snapshot: " + file.getAbsolutePath());
                deleted++;
            } else if (file != null) {
                GoodLogger.debug("Failed to delete: " + file.getAbsolutePath());
            }
        }
    }

    public static void deleteOldPlayers(File folder, String name) {
        Integer max = (Integer) ConfigManager.getConfig(name);
        if (max == null) max = 10;

        if (max == 0) return; // Keep all

        List<File> playerDirs = new ArrayList<>();
        Map<FileTime, File> timeToFile = new HashMap<>();
        List<FileTime> creationTimes = new ArrayList<>();

        // Read all player directories and their creation times
        try (Stream<Path> stream = Files.list(folder.toPath())) {
            stream.filter(Files::isDirectory)
                    .forEach(path -> {
                        try {
                            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
                            FileTime creationTime = attr.creationTime();
                            File file = path.toFile();

                            playerDirs.add(file);
                            timeToFile.put(creationTime, file);
                            creationTimes.add(creationTime);
                        } catch (IOException e) {
                            GoodLogger.warn("Could not read attributes for: " + path.getFileName());
                        }
                    });
        } catch (IOException e) {
            GoodLogger.warn("Error reading directory: " + e.getMessage());
            return;
        }
        if (playerDirs.size() <= max) return;
        creationTimes.sort(FileTime::compareTo);
        int toDelete = playerDirs.size() - max;
        int deleted = 0;

        for (FileTime time : creationTimes) {
            if (deleted >= toDelete) break;

            File playerDir = timeToFile.get(time);
            if (playerDir == null) continue;

            // Delete directory recursively
            if (deleteFolder(playerDir)) {
                GoodLogger.debug("Deleted old player data: " + playerDir.getAbsolutePath());
                deleted++;
            } else {
                GoodLogger.warn("Failed to delete: " + playerDir.getAbsolutePath());
            }
        }
    }

    public static boolean writeTimeAndMessage(File snapshot, UUID uuid, String message, String key){
        CompoundTag tag = getPlayerSnapshotData(snapshot);
        ListTag listTag = new ListTag();
        listTag.add(Serializer.serializeString(message, key));
        listTag.add(Serializer.serializeString(Instant.now().toString(), "time"));

        try{
            NbtIo.writeCompressed(tag, snapshot.toPath());
            return true;
        } catch(IOException e){
            GoodLogger.error("Failed to save player data for " + uuid.toString() + ": \n" + e.getMessage());
            try{
                Files.delete(snapshot.toPath());
            } catch(IOException e1){
                GoodLogger.error("Failed to delete failed snapshot file: " + snapshot.getName() + "\n Because: " + e.getMessage());
            }
            return false;
        }
    }

    public static File makeNewSnapshot(File folder, String name){
        File newSnapshot;
        if(name == null){
            LocalDateTime now = LocalDateTime.now();
            newSnapshot =  new File(folder.toPath() +
                    File.separator + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".nahumbackup");

            for(int i = 1; newSnapshot.exists(); i++){
                newSnapshot = new File(folder.toPath() +
                        File.separator + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) +
                        "-(" + i + ")" + ".nahumbackup");
            }
            NahumInventoryStuff.deleteOldBackups(folder);
        } else {
            newSnapshot = new File(folder.toPath().toString(), name + ".nahumbackup");
        }
        return newSnapshot;
    }

    public static boolean performPlayerSaveSnapshot(Object player, ItemStack[] inventory, ItemStack[] echest, String message, String name){
        UUID uuid = DataParser.getUuidFromObject(player);
        File folder = getPlayerDeathsFolder(uuid);

        if(uuid == null){
            GoodLogger.warn("Couldn't get last dead player's uuid");
            return false;
        }

        File snapshot = makeNewSnapshot(folder, null);
        saveSnapshot(inventory, echest, uuid, snapshot);
        deleteOldSnapshots(folder, name);

        if(!writeTimeAndMessage(snapshot, uuid, message, "message")){
            GoodLogger.warn("Couldn't save player's snapshot");
            return false;
        }

        name = name.toLowerCase().replace("max", "");
        name = name.replace("snapshot", "");

        GoodLogger.debug(((OfflinePlayer)player).getName() + "'s " + name + " snapshot saved to: " + snapshot.getAbsolutePath());
        return true;
    }

    public static String performAdminBufferSave(
            Object admin, Object player, ItemStack[] inventory, ItemStack[] echest, String message, String name, String otherPath
    ){
        if(name==null){
            name = "maxSnapshots";
        }
        UUID playerUuid = DataParser.getUuidFromObject(player);
        File folder = getPlayerBuffer(admin, player);

        if(playerUuid == null){
            GoodLogger.warn("Couldn't get last dead player's uuid");
            return null;
        }

        File snapshot = makeNewSnapshot(folder, null);
        saveSnapshot(inventory, echest, playerUuid, snapshot);
        deleteOldSnapshots(folder, name);
        deleteOldPlayers(folder.getParentFile(), name);

        if(!writeTimeAndMessage(snapshot, playerUuid, message, "message")){
            GoodLogger.warn("Couldn't save player's snapshot");
            return null;
        }

        if(otherPath != null){
            writeString(snapshot, playerUuid, otherPath, "linkedTo");
        }

        GoodLogger.debug(((OfflinePlayer)admin).getName() + "'s actions on " + ((OfflinePlayer)player).getName() + "'s inventories saved to: " + snapshot.getAbsolutePath());
        return snapshot.getAbsolutePath();
    }

    public static boolean updateSnapshot(Path path, Object admin, Object player, String toWrite, String key){
        File snapshot = path.toFile();
        UUID playerUuid = DataParser.getUuidFromObject(player);
        return writeString(snapshot, playerUuid, toWrite, key);
    }
}