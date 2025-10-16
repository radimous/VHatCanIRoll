package com.radimous.vhatcaniroll.logic;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModDynamicModels;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.init.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

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
            withTransmog(new ItemStack(ModItems.SWORD), new ResourceLocation("the_vault:gear/sword/sword_0")),
            withTransmog(new ItemStack(ModItems.AXE), new ResourceLocation("the_vault:gear/axe/axe_0")),
            withTransmog(new ItemStack(ModItems.HELMET), new ResourceLocation("the_vault:gear/armor/gladiator_dark/helmet")),
            withTransmog(new ItemStack(ModItems.CHESTPLATE), new ResourceLocation("the_vault:gear/armor/magmatic/chestplate")),
            withTransmog(new ItemStack(ModItems.LEGGINGS), new ResourceLocation("the_vault:gear/armor/reinforced_platemail_dark/leggings")),
            withTransmog(new ItemStack(ModItems.BOOTS), new ResourceLocation("the_vault:gear/armor/gladiator_dark/boots")),
            withTransmog(new ItemStack(ModItems.FOCUS), new ResourceLocation("the_vault:gear/focus/tatteredtome")),
            withTransmog(new ItemStack(ModItems.SHIELD), new ResourceLocation("the_vault:gear/shield/gold_plated")),
            withTransmog(new ItemStack(ModItems.WAND), new ResourceLocation("the_vault:gear/wand/lunar")),
            withTransmog(new ItemStack(ModItems.MAGNET), new ResourceLocation("the_vault:magnets/magnet_1")),
            withTransmog(new ItemStack(ModItems.JEWEL), new ResourceLocation("the_vault:gear/jewel/sword_0")),
            withTransmog(new ItemStack(ModItems.VOID_STONE), new ResourceLocation("the_vault:gear/necklace/default")),
//            withTransmog(new ItemStack(ModItems.VAULT_NECKLACE), new ResourceLocation("the_vault:gear/necklace/default")),
            godCharm(new ItemStack(ModItems.VAULT_GOD_CHARM), VaultGod.IDONA, VaultGearRarity.COMMON),
            godCharm(new ItemStack(ModItems.VAULT_GOD_CHARM), VaultGod.TENOS, VaultGearRarity.RARE),
            godCharm(new ItemStack(ModItems.VAULT_GOD_CHARM), VaultGod.VELARA, VaultGearRarity.EPIC),
            godCharm(new ItemStack(ModItems.VAULT_GOD_CHARM), VaultGod.WENDARR, VaultGearRarity.OMEGA)

        );
    }

    public static List<ItemStack> getWoldGearItems() {
        List<ItemStack> woldItems = new ArrayList<>();
        List<Pair<String, String>> woldItemFields = Arrays.asList(
            Pair.of("BATTLESTAFF", "the_vault:gear/battlestaff/battlestaff_redstone"),
            Pair.of("TRIDENT", "the_vault:gear/trident/orange"),
            Pair.of("PLUSHIE", "the_vault:gear/plushie/hrry"),
            Pair.of("LOOT_SACK", "the_vault:gear/loot_sack/bundle"),
            Pair.of("RANG", "the_vault:gear/rang/wooden"),
            Pair.of("MAP", "the_vault:gear/map/common")
        );
        try{
            Class<?> woldItemClass = Class.forName("xyz.iwolfking.woldsvaults.init.ModItems");
            for (Pair<String, String> woldFieldTransmogs : woldItemFields) {
                try {
                    String woldFieldName = woldFieldTransmogs.getLeft();
                    Item item = (Item) woldItemClass.getField(woldFieldName).get(null);
                    woldItems.add(withTransmog(new ItemStack(item), new ResourceLocation(woldFieldTransmogs.getRight())));
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

    /**
     * Creates copy of the given stack with the transmog applied
     * @param stack the stack to apply the transmog to
     * @param transmog the transmog to apply
     * @return the stack with the transmog applied
     */
    public static ItemStack withTransmog(ItemStack stack, ResourceLocation transmog){
        ItemStack displayStack = stack.copy();
        VaultGearData gearData = VaultGearData.read(displayStack);
        gearData.setState(VaultGearState.IDENTIFIED);
        gearData.createOrReplaceAttributeValue(ModGearAttributes.GEAR_MODEL, transmog);
        gearData.write(displayStack);
        return displayStack;
    }

    public static ItemStack godCharm(ItemStack stack, VaultGod god, VaultGearRarity rarity) {
        ItemStack displayStack = stack.copy();
        VaultGearData gearData = VaultGearData.read(displayStack);
        gearData.setState(VaultGearState.IDENTIFIED);
        gearData.createOrReplaceAttributeValue(ModGearAttributes.CHARM_VAULT_GOD, god);
        gearData.setRarity(rarity);
        ResourceLocation id = ModDynamicModels.VaultGodCharms.getModel(gearData.getFirstValue(ModGearAttributes.CHARM_VAULT_GOD).orElse(VaultGod.IDONA), rarity).getId();
        gearData.createOrReplaceAttributeValue(ModGearAttributes.GEAR_MODEL, id);
        gearData.write(displayStack);
        return displayStack;
    }
}
