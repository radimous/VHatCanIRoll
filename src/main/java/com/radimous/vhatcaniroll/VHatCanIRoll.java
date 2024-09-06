package com.radimous.vhatcaniroll;

import iskallia.vault.init.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Mod("vhatcaniroll")
public class VHatCanIRoll {

    public static final String MODID = "vhatcaniroll";
    private static final List<ItemStack> GEAR_ITEMS = new ArrayList<>();
    public VHatCanIRoll() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static List<ItemStack> getVaultGearItems() {
        if (GEAR_ITEMS.isEmpty()){
            GEAR_ITEMS.addAll(List.of(
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
                )
            );
            GEAR_ITEMS.addAll(getWoldGearItems());

        }
        return Collections.unmodifiableList(GEAR_ITEMS);
    }

    public static List<ItemStack> getWoldGearItems() {
        List<ItemStack> woldItems = new ArrayList<>();
        List<String> woldItemFields = Arrays.asList(
            "BATTLESTAFF",
            "TRIDENT",
            "PLUSHIE",
            "LOOT_SACK"
        );
        try{
            Class<?> wold = Class.forName("xyz.iwolfking.woldsvaults.init.ModItems");
            for (String name : woldItemFields) {
                try {
                    Item item = (Item) wold.getField(name).get(null);
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
