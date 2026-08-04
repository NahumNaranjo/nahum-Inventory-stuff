package dev.nahum.nahumInventoryStuff;

import net.minecraft.nbt.NbtAccounter;
import org.bukkit.Bukkit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class FileManager {
    final static NbtAccounter ACCOUNTER = NbtAccounter.create(32768 * 1024);

    static public File getWorldDir(){
        return Bukkit.getWorlds().getFirst().getWorldFolder();
    }
    static public File getPlayerFolder(){
        return new File(getWorldDir(), "playerdata");
    }

    static public File getPlayerFile(UUID uuid) {
        return new File(getPlayerFolder(), uuid.toString() + ".dat");
    }

    static public CompoundTag getPlayerData(UUID uuid){
        File file = getPlayerFile(uuid);
        try{
            return NbtIo.readCompressed(file.toPath(), ACCOUNTER);
        } catch (IOException e) {
            System.err.println("Failed to read player data!" + "\nError: " + e.getMessage());
            return null;
        }
    }
}
