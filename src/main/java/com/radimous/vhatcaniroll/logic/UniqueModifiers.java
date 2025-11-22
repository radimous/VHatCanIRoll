package com.radimous.vhatcaniroll.logic;

import com.radimous.vhatcaniroll.logic.modifiervalues.ModifierValues;
import iskallia.vault.config.UniqueGearConfig;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.init.ModConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UniqueModifiers {
    public static List<Component> getUniqueModifierList(int lvl, ModifierCategory modifierCategory, Map<UniqueGearConfig.AffixTargetType, List<ResourceLocation>> modifierIdentifiers) {
        ArrayList<Component> modList = new ArrayList<>();

        for (Map.Entry<UniqueGearConfig.AffixTargetType, List<ResourceLocation>> modifierIdentifier : modifierIdentifiers.entrySet()) {
            modList.addAll(getUniqueAffixComponents(lvl, modifierIdentifier, modifierCategory));
        }

        return modList;
    }

    /**
     * Same as getAffixGroupComponents, but for unique gear without chances and groups
     */
    public static List<Component> getUniqueAffixComponents(int lvl,Map.Entry<UniqueGearConfig.AffixTargetType, List<ResourceLocation>> modifierIdentifier, ModifierCategory modifierCategory) {

        ArrayList<Component> componentList = new ArrayList<>();
        if (modifierIdentifier.getValue().isEmpty()) {
            return componentList; // no affix for this type
        }
        UniqueGearConfig.AffixTargetType affixTagGroup = modifierIdentifier.getKey();
        componentList.add(new TextComponent(affixTagGroup.toString().replace("_", " ")).withStyle(ChatFormatting.BOLD));
        for (ResourceLocation modifier : modifierIdentifier.getValue()) {
            VaultGearTierConfig.ModifierTierGroup modifierTierGroup = ModConfigs.VAULT_GEAR_CONFIG.get(VaultGearTierConfig.UNIQUE_ITEM).getTierGroup(modifier);
            if (modifierTierGroup == null) {
                continue;
            }
            ArrayList<VaultGearTierConfig.ModifierTier<?>> mTierList;
            mTierList = Modifiers.getModifierTiers(lvl, modifierTierGroup, modifierCategory);
            if (mTierList.isEmpty()) {
                continue;
            }
            MutableComponent
                modComp = ModifierValues.getModifierComponent(VaultGearAttributeRegistry.getAttribute(modifierTierGroup.getAttribute()),mTierList);

            MutableComponent full = new TextComponent("  ");
            full.append(modComp);
            componentList.add(full);
        }

        if (componentList.size() == 1) { // only header
            return new ArrayList<>(); // no affixes for this type (all tiers are unobtainable)
        }

        componentList.add(TextComponent.EMPTY);
        return componentList;
    }
}
