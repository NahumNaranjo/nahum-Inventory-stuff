package dev.nahum.nahumInventoryStuff;

public class NbtTags {
    private static String echest = "EnderItems";
    private static String inventory = "Inventory";
    private static String slot = "Slot";
    private static String count = "count";
    private static String id = "id";
    private static String components = "components";
    private static String equipment = "equipment";
    private static String head = "head";
    private static String chest = "chest";
    private static String legs = "legs";
    private static String feet = "feet";
    private static String offhand = "offhand";
    private static String[] armor = {head, feet, chest, legs, offhand};

    public static String getEchest() {return echest;}
    public static String getInventory() {return inventory;}
    public static String getSlot() {return slot;}
    public static String getCount() {return count;}
    public static String getId() {return id;}
    public static String getComponents() {return components;}
    public static String getEquipment() {return equipment;}
    public static String getHead() {return head;}
    public static String getChest() {return chest;}
    public static String getLegs() {return legs;}
    public static String getFeet() {return feet;}
    public static String getOffhand() {return offhand;}
    public static String[] getArmor() {return armor;}
}
