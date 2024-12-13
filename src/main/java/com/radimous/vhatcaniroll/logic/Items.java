package com.radimous.vhatcaniroll.logic;

import iskallia.vault.init.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Items {

    private static final List<ItemStack> GEAR_ITEMS = new ArrayList<>();


    public static List<ItemStack> getVaultGearItems() {
        if (GEAR_ITEMS.isEmpty()){
            GEAR_ITEMS.addAll(getVHGearItems());
            GEAR_ITEMS.addAll(getWoldGearItems());
        }
        return Collections.unmodifiableList(GEAR_ITEMS);
    }

    public static List<ItemStack> getVHGearItems() {
        return List.of(
                    new ItemStack(ModItems.SWORD),
                    new ItemStack(ModItems.AXE),
                    new ItemStack(ModItems.HELMET),
                    new ItemStack(ModItems.CHESTPLATE),
                    new ItemStack(ModItems.LEGGINGS),
                    new ItemStack(ModItems.BOOTS),
                    new ItemStack(ModItems.FOCUS),
                    new ItemStack(ModItems.SHIELD),
                    new ItemStack(ModItems.WAND),
                    new ItemStack(ModItems.MAGNET),
                    new ItemStack(ModItems.JEWEL)
                );        
    }

    public static List<ItemStack> getWoldGearItems() {
        List<ItemStack> woldItems = new ArrayList<>();
        List<String> woldItemFields = Arrays.asList(
            "BATTLESTAFF",
            "TRIDENT",
            "PLUSHIE",
            "LOOT_SACK",
            "RANG"
        );
        try{
            Class<?> woldItemClass = Class.forName("xyz.iwolfking.woldsvaults.init.ModItems");
            for (String woldFieldName : woldItemFields) {
                try {
                    Item item = (Item) woldItemClass.getField(woldFieldName).get(null);
                    woldItems.add(new ItemStack(item));
                } catch (IllegalArgumentException | SecurityException | NoSuchFieldException |
                         IllegalAccessException ignored) {
                    // no-op
                }
            }
        } catch (ClassNotFoundException ignored) {
            // no-op
        }
        return woldItems;

    }
}
