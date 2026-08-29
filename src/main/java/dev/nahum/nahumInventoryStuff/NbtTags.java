package dev.nahum.nahumInventoryStuff;

public class NbtTags {
    // minecraft
    private static final String echest = "EnderItems";
    private static final String inventory = "Inventory";
    private static final String slot = "Slot";
    private static final String count = "count";
    private static final String id = "id";
    private static final String components = "components";
    private static final String equipment = "equipment";
    private static final String head = "head";
    private static final String chest = "chest";
    private static final String legs = "legs";
    private static final String feet = "feet";
    private static final String offhand = "offhand";
    private static final String[] armor = {head, feet, chest, legs, offhand};

    //  custom
    private static final String linkedTo = "linkedTo";

    public static String getLinkedTo() {return linkedTo;}

    public static String getEchest() {
        return echest;
    }

    public static String getInventory() {
        return inventory;
    }

    public static String getSlot() {
        return slot;
    }

    public static String getCount() {
        return count;
    }

    public static String getId() {
        return id;
    }

    public static String getComponents() {
        return components;
    }

    public static String getEquipment() {
        return equipment;
    }

    public static String getHead() {
        return head;
    }

    public static String getChest() {
        return chest;
    }

    public static String getLegs() {
        return legs;
    }

    public static String getFeet() {
        return feet;
    }

    public static String getOffhand() {
        return offhand;
    }

    public static String[] getArmor() {
        return armor;
    }
}
