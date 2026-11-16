package com.radimous.vhatcaniroll.logic;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModDynamicModels;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.init.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class Items {

    private static final String IMC_MSG = "PLEASE_USE_IMC_INSTEAD_OF_MIXIN";
    private static final List<ItemStack> GEAR_ITEMS = new ArrayList<>();
    private static int lastIMCSize = 0;
    private static final Map<RegistrationInfo, List<ItemStack>> IMC_REGISTRATIONS = new LinkedHashMap<>();


    public static List<ItemStack> getVaultGearItems() {
        if (IMC_REGISTRATIONS.size() != lastIMCSize) {
            GEAR_ITEMS.clear();
            lastIMCSize = IMC_REGISTRATIONS.size();
        }
        if (GEAR_ITEMS.isEmpty()){
            GEAR_ITEMS.addAll(getVHGearItems());
            GEAR_ITEMS.addAll(getIMCItems());
        }
        return Collections.unmodifiableList(GEAR_ITEMS);
    }

    public static List<ItemStack> getVHGearItems() {
        return List.of(
            withTransmog(new ItemStack(ModItems.SWORD), ResourceLocation.parse("the_vault:gear/sword/sword_0")),
            withTransmog(new ItemStack(ModItems.AXE), ResourceLocation.parse("the_vault:gear/axe/axe_0")),
            withTransmog(new ItemStack(ModItems.HELMET), ResourceLocation.parse("the_vault:gear/armor/gladiator_dark/helmet")),
            withTransmog(new ItemStack(ModItems.CHESTPLATE), ResourceLocation.parse("the_vault:gear/armor/magmatic/chestplate")),
            withTransmog(new ItemStack(ModItems.LEGGINGS), ResourceLocation.parse("the_vault:gear/armor/reinforced_platemail_dark/leggings")),
            withTransmog(new ItemStack(ModItems.BOOTS), ResourceLocation.parse("the_vault:gear/armor/gladiator_dark/boots")),
            withTransmog(new ItemStack(ModItems.FOCUS), ResourceLocation.parse("the_vault:gear/focus/tatteredtome")),
            withTransmog(new ItemStack(ModItems.SHIELD), ResourceLocation.parse("the_vault:gear/shield/gold_plated")),
            withTransmog(new ItemStack(ModItems.WAND), ResourceLocation.parse("the_vault:gear/wand/lunar")),
            withTransmog(new ItemStack(ModItems.MAGNET), ResourceLocation.parse("the_vault:magnets/magnet_1")),
            withTransmog(new ItemStack(ModItems.JEWEL), ResourceLocation.parse("the_vault:gear/jewel/sword_0")),
            withTransmog(new ItemStack(ModItems.VAULT_NECKLACE), ResourceLocation.parse("the_vault:gear/necklace/default")),
            godCharm(new ItemStack(ModItems.VAULT_GOD_CHARM), VaultGod.IDONA, VaultGearRarity.COMMON),
            godCharm(new ItemStack(ModItems.VAULT_GOD_CHARM), VaultGod.TENOS, VaultGearRarity.RARE),
            godCharm(new ItemStack(ModItems.VAULT_GOD_CHARM), VaultGod.VELARA, VaultGearRarity.EPIC),
            godCharm(new ItemStack(ModItems.VAULT_GOD_CHARM), VaultGod.WENDARR, VaultGearRarity.OMEGA)
        );
    }

    // DON'T INVOKE THIS METHOD FROM DIFFERENT MODS
    public static void registerIMCItems(String sender, Integer order, List<ItemStack> itemList) {
       IMC_REGISTRATIONS.put(new RegistrationInfo(order, sender), itemList);
    }

    private static List<ItemStack> getIMCItems() {
        return IMC_REGISTRATIONS.entrySet()
            .stream()
            .sorted(
                Comparator
                    .comparing(
                        (Map.Entry<RegistrationInfo, List<ItemStack>> e) -> e.getKey().order()
                    )
                    .thenComparing(e -> e.getKey().modid())
            )
            .flatMap(entry -> entry.getValue().stream())
            .map(ItemStack::copy)
            .collect(Collectors.toList());
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
    public record RegistrationInfo(Integer order, String modid){}
}
