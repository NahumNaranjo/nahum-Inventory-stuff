package dev.nahum.nahumInventoryStuff;

import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Serializer {

    static final int INVENTORYSIZE = 41;
    static final int MAIN_INVENTORY_SIZE = 36;
    static final int ECHESTSIZE = 27;
    static final int ARMORSIZE = 4;
    static final int ARMOR_START = 36;
    static final int OFFHAND_SLOT = 40;

    public static World getWorld() {return Bukkit.getWorlds().getFirst();}
    public static RegistryAccess getRegistry(){return ((CraftWorld) getWorld()).getHandle().registryAccess();}
    public static RegistryFriendlyByteBuf getBuf(){return new RegistryFriendlyByteBuf(Unpooled.buffer(), getRegistry());}
    public static RegistryOps getOps() { return getRegistry().createSerializationContext(JsonOps.INSTANCE);}

    private static RegistryOps<net.minecraft.nbt.Tag> registryNbtOps() {
        return RegistryOps.create(NbtOps.INSTANCE, getRegistry());
    }

    public static CompoundTag serializeItem(ItemStack item) {
        net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(item);
        net.minecraft.nbt.Tag itemNbt = net.minecraft.world.item.ItemStack.CODEC
                .encodeStart(registryNbtOps(), nms)
                .getOrThrow();
        if (itemNbt instanceof CompoundTag itemTag) {
            return itemTag;
        }
        throw new IllegalStateException("ItemStack codec did not produce a compound tag");
    }

    public static ItemStack deserializeItem(CompoundTag itemTag) {
        net.minecraft.world.item.ItemStack nmsItem = net.minecraft.world.item.ItemStack.CODEC
                .parse(registryNbtOps(), itemTag)
                .getOrThrow(IllegalArgumentException::new);
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    public static ListTag serializeToListTag(ItemStack[] itemStack, OfflinePlayer offlinePlayer){
        return serializeToListTag(itemStack, offlinePlayer == null ? "unknown" : offlinePlayer.getName());
    }

    public static ListTag serializeToListTag(ItemStack[] itemStack){
        return serializeToListTag(itemStack, "unknown");
    }

    public static CompoundTag serializeString(String message, String key){
        CompoundTag tag = new CompoundTag();
        tag.putString(key, message);
        return tag;
    }

    public static CompoundTag serializeArmorToCompoundTag(ItemStack[] itemStack){
        CompoundTag returning = new CompoundTag();

        for(int i = 0; i < itemStack.length; i++){
            try{
                ItemStack item = itemStack[i];

                if(item == null || item.getType().isAir()){
                    continue;
                }

                CompoundTag tag = new CompoundTag();
                tag.putInt(NbtTags.getCount(), 1);
                Material mat = item.getType();
                String id = mat.getKey().toString();
                tag.putString(NbtTags.getId(), id);
                EquipmentSlot defaultSlot = mat.getEquipmentSlot();
                GoodLogger.debug("Slot is " + defaultSlot.name());

                if(defaultSlot == EquipmentSlot.HEAD ){
                    GoodLogger.debug("Slot was " + EquipmentSlot.HEAD.name());
                    returning.put(NbtTags.getHead(), tag);
                } else if(defaultSlot == EquipmentSlot.CHEST ){
                    GoodLogger.debug("Slot was " + EquipmentSlot.CHEST.name());
                    returning.put(NbtTags.getChest(), tag);
                } else if(defaultSlot == EquipmentSlot.LEGS ){
                    GoodLogger.debug("Slot was " + EquipmentSlot.LEGS.name());
                    returning.put(NbtTags.getLegs(), tag);
                }else if(defaultSlot == EquipmentSlot.FEET ){
                    GoodLogger.debug("Slot was " + EquipmentSlot.FEET.name());
                    returning.put(NbtTags.getFeet(), tag);
                } else {
                    GoodLogger.debug("Slot was " + EquipmentSlot.OFF_HAND.name());
                    returning.put(NbtTags.getOffhand(), tag);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return returning;
    }
    private static ListTag serializeToListTag(ItemStack[] itemStack, String contextName){
        ListTag listTag = new ListTag();

        for (int slot = 0; slot < itemStack.length; slot++) {
            try {
                ItemStack item = itemStack[slot];

                if (item == null || item.getType().isAir()) {
                    continue;
                }

                CompoundTag itemTag = serializeItem(item);
                itemTag.putByte(NbtTags.getSlot(), (byte) slot);
                listTag.add(itemTag);
                GoodLogger.debug("NBT slot: " + slot + "\nItem: " + itemTag);
            } catch (Exception e) {
                GoodLogger.error("Error serializing ItemStack for " + contextName + "\nError: " + e);
            }
        }
        return listTag;
    }

    public static ItemStack[] deserializeFromListTag(ListTag listTag, int size) {
        ItemStack[] itemStacks = new ItemStack[size];
        boolean hasSlotTags = false;

        for (int i = 0; i < listTag.size(); i++) {
            if (listTag.getCompoundOrEmpty(i).contains(NbtTags.getSlot())) {
                hasSlotTags = true;
                break;
            }
        }

        for (int i = 0; i < listTag.size(); i++) {
            final int listIndex = i;
            try {
                CompoundTag itemTag = listTag.getCompoundOrEmpty(listIndex);
                int slot;
                if (hasSlotTags) {
                    slot = itemTag.getByte(NbtTags.getSlot())
                            .map(Byte::intValue)
                            .orElseGet(() -> legacySlotForListIndex(listTag, listIndex, size));
                } else {
                    slot = legacySlotForListIndex(listTag, listIndex, size);
                }

                ItemStack item = deserializeItem(itemTag);

                if (slot >= 0 && slot < size) {
                    itemStacks[slot] = item;
                }
                GoodLogger.debug("NBT slot: " + slot + "\nItem: " + itemTag);
            } catch (Exception e) {
                GoodLogger.error("Error deserializing single item tag: " + e.getMessage());
            }
        }
        return itemStacks;
    }

    private static int legacySlotForListIndex(ListTag listTag, int listIndex, int size) {
        if (size == ECHESTSIZE) {
            return listIndex;
        }

        if (size != INVENTORYSIZE) {
            return listIndex;
        }

        int itemCount = listTag.size();
        if (itemCount <= 1) {
            return 0;
        }

        if (itemCount <= 6) {
            if (listIndex == 0) {
                return 0;
            }
            return ARMOR_START + listIndex - 1;
        }

        int mainItems = itemCount - 5;
        if (listIndex < mainItems) {
            return listIndex;
        }
        return ARMOR_START + (listIndex - mainItems);
    }

    public static ListTag serializeMainInventoryListTag(ItemStack[] fullInventory) {
        ItemStack[] mainInventory = new ItemStack[MAIN_INVENTORY_SIZE];
        System.arraycopy(fullInventory, 0, mainInventory, 0, MAIN_INVENTORY_SIZE);
        return serializeToListTag(mainInventory);
    }

    public static CompoundTag serializeEquipmentCompound(ItemStack[] fullInventory) {
        CompoundTag equipment = new CompoundTag();
        putEquipmentItem(equipment, NbtTags.getFeet(), fullInventory[ARMOR_START]);
        putEquipmentItem(equipment, NbtTags.getLegs(), fullInventory[ARMOR_START + 1]);
        putEquipmentItem(equipment, NbtTags.getChest(), fullInventory[ARMOR_START + 2]);
        putEquipmentItem(equipment, NbtTags.getHead(), fullInventory[ARMOR_START + 3]);
        putEquipmentItem(equipment, NbtTags.getOffhand(), fullInventory[OFFHAND_SLOT]);
        return equipment;
    }

    private static void putEquipmentItem(CompoundTag equipment, String key, ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return;
        }
        equipment.put(key, serializeItem(item));
    }

    public static ItemStack[] buildFullInventoryFromPlayerTag(CompoundTag tag) {
        ItemStack[] fullInventory = new ItemStack[INVENTORYSIZE];

        ListTag inventoryList = tag.getListOrEmpty(NbtTags.getInventory());
        for (int i = 0; i < inventoryList.size(); i++) {
            try {
                CompoundTag itemTag = inventoryList.getCompoundOrEmpty(i);
                int slot = itemTag.getByte(NbtTags.getSlot()).map(Byte::intValue).orElse(0);
                if (slot < 0 || slot >= MAIN_INVENTORY_SIZE) {
                    continue;
                }
                fullInventory[slot] = deserializeItem(itemTag);
            } catch (Exception e) {
                GoodLogger.error("Failed to read inventory item from player data: " + e.getMessage());
            }
        }

        CompoundTag equipment = tag.getCompoundOrEmpty(NbtTags.getEquipment());
        applyEquipmentItem(equipment, NbtTags.getFeet(), fullInventory, ARMOR_START);
        applyEquipmentItem(equipment, NbtTags.getLegs(), fullInventory, ARMOR_START + 1);
        applyEquipmentItem(equipment, NbtTags.getChest(), fullInventory, ARMOR_START + 2);
        applyEquipmentItem(equipment, NbtTags.getHead(), fullInventory, ARMOR_START + 3);
        applyEquipmentItem(equipment, NbtTags.getOffhand(), fullInventory, OFFHAND_SLOT);

        return fullInventory;
    }

    private static void applyEquipmentItem(CompoundTag equipment, String key, ItemStack[] fullInventory, int slot) {
        if (!equipment.contains(key)) {
            return;
        }
        try {
            fullInventory[slot] = deserializeItem(equipment.getCompoundOrEmpty(key));
        } catch (Exception e) {
            GoodLogger.error("Failed to read equipment item " + key + ": " + e.getMessage());
        }
    }

    public static void applyFullInventoryToPlayer(CompoundTag playerTag, ItemStack[] fullInventory) {
        playerTag.put(NbtTags.getInventory(), serializeMainInventoryListTag(fullInventory));
        playerTag.put(NbtTags.getEquipment(), serializeEquipmentCompound(fullInventory));
    }

    public static Inventory deserializeToEchest(ListTag list, OfflinePlayer offlinePlayer){
        Inventory inv = Bukkit.createInventory(null, InventoryType.ENDER_CHEST, "Inventory");

        for (int slot = 0; slot < list.size(); slot++) {
            try{
                CompoundTag compoundTag = new CompoundTag();

            } catch (Exception e){

                return inv;
            }
        }
        return inv;
    }

}
