package com.radimous.vhatcaniroll.logic.modifiervalues;

import com.radimous.vhatcaniroll.logic.ComponentUtil;
import com.radimous.vhatcaniroll.mixin.accessors.EffectConfigAccessor;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.ability.AbilityLevelAttribute;
import iskallia.vault.gear.attribute.ability.special.base.SpecialAbilityGearAttribute;
import iskallia.vault.gear.attribute.config.ConfigurableAttributeGenerator;
import iskallia.vault.gear.attribute.custom.effect.EffectGearAttribute;
import iskallia.vault.gear.attribute.talent.TalentLevelAttribute;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;

import java.util.ArrayList;

import static com.radimous.vhatcaniroll.logic.modifiervalues.SpecialModifiers.*;

public class ModifierValues {

    @SuppressWarnings("unchecked") // I don't think proper generics are possible, VaultGearTierConfig#getModifiersForLevel returns List<ModifierTier<?>>
    public static <T, C> MutableComponent getModifierComponent(VaultGearAttribute<T> atr,
                                                               ArrayList<VaultGearTierConfig.ModifierTier<?>> modifierTiers) {
        if (modifierTiers.isEmpty()) {
            return new TextComponent("ERR - EMPTY MODIFIER TIERS").withStyle(ChatFormatting.RED);
        }

        if (atr == null) {
            return new TextComponent("ERR - NULL ATTRIBUTE").withStyle(ChatFormatting.RED);
        }

        ResourceLocation atrRegName = atr.getRegistryName();
        if (atrRegName == null) {
            return new TextComponent("ERR - NULL REGISTRY NAME").withStyle(ChatFormatting.RED);
        }
        String atrName = atrRegName.toString();

        ConfigurableAttributeGenerator<T, C> atrGenerator = (ConfigurableAttributeGenerator<T, C>) atr.getGenerator();
        if (atrGenerator == null) {
            return new TextComponent("ERR - NULL ATTRIBUTE GENERATOR - " + atrName).withStyle(ChatFormatting.RED);
        }

        C minConfig = (C) modifierTiers.get(0).getModifierConfiguration();
        C maxConfig = (C) modifierTiers.get(modifierTiers.size() - 1).getModifierConfiguration();

        MutableComponent minConfigDisplay = atrGenerator.getConfigDisplay(atr.getReader(), minConfig);
        MutableComponent maxConfigDisplay = atrGenerator.getConfigDisplay(atr.getReader(), maxConfig);

        // CLOUDS
        if (minConfigDisplay != null && maxConfigDisplay != null && (atrName.equals("the_vault:effect_cloud") || atrName.equals("the_vault:effect_cloud_when_hit"))) {
            return getCloudRangeComponent(minConfigDisplay, maxConfigDisplay, atr);
        }
        if (minConfig instanceof EffectGearAttribute.Config minEffectConfig
            && maxConfig instanceof EffectGearAttribute.Config
            && maxConfigDisplay != null) {
            String effectStr = ((EffectConfigAccessor)minEffectConfig).getAmplifier() + "-" +
                maxConfigDisplay.getString();
            return new TextComponent(effectStr).withStyle(atr.getReader().getColoredTextStyle());
        }

        // SPECIAL ABILITY TIER
        if (minConfig instanceof SpecialAbilityGearAttribute.SpecialAbilityTierConfig<?,?,?> minConfigSpecial) {
            return getSpecialAbilityAttributeComponent(modifierTiers, minConfigSpecial);
        }

        // CUSTOM (UNIQUES)
        var customComponent = getCustomComponent(atr, minConfig, maxConfig, atrName, atrGenerator);
        if (customComponent != null) {
            return customComponent;
        }

        // FINALLY MOSTLY NORMAL ATTRIBUTES (thank god)
        if (modifierTiers.size() > 1) {
            MutableComponent res = rangeComponent(atrName, atr, atrGenerator, minConfig, maxConfig);
            if (res != null) {
                return ComponentUtil.simplifyMatchingValues(res);
            }
        }
        if (minConfigDisplay != null) { // except few
            MutableComponent res = minConfigDisplay.withStyle(atr.getReader().getColoredTextStyle());
            if (minConfig instanceof AbilityLevelAttribute.Config minConfigAbility) {
                return abilityLvlComponent(res, atr, minConfigAbility);
            }
            if (minConfig instanceof TalentLevelAttribute.Config minConfigTalent) {
                return talentLvlComponent(res, atr, minConfigTalent);
            }
            if (minConfig instanceof EffectGearAttribute.Config ) {
                return minConfigDisplay;
            }
            return ComponentUtil.simplifyMatchingValues(res);
        }
        return new TextComponent("ERR - NULL DISPLAY " + atrName);
    }


    /**
     * This method handles combining multiple configs into a single component
     * VH doesn't have method for this, so we need to do it manually
     * it is using the same logic as VH does when shifting on gear piece to get the range
     * and combining it with normal display for single component (that has name and color)
     */
    private static <T, C> MutableComponent rangeComponent(String atrName, VaultGearAttribute<T> atr,
                                                          ConfigurableAttributeGenerator<T, C> atrGenerator, C minConfig, C maxConfig) {
        MutableComponent res = atrGenerator.getConfigRangeDisplay(atr.getReader(), minConfig, maxConfig);
        MutableComponent minConfigDisplay = atrGenerator.getConfigDisplay(atr.getReader(), minConfig);


        if (res != null && minConfig instanceof AbilityLevelAttribute.Config minConfigAbility) {
            return abilityLvlComponent(res, atr, minConfigAbility);
        }

        if (res != null && minConfig instanceof TalentLevelAttribute.Config minConfigTalent) {
            return talentLvlComponent(res, atr, minConfigTalent);
        }

        if ((atrName.equals("the_vault:effect_avoidance") || atrName.equals("the_vault:effect_list_avoidance")) && minConfigDisplay != null) {
            // res -> "30% - 50%"
            // single ->  "30% Poison Avoidance"
            // minRange -> "30%"
            MutableComponent single = minConfigDisplay.withStyle(atr.getReader().getColoredTextStyle());
            MutableComponent minRange = atrGenerator.getConfigRangeDisplay(atr.getReader(), minConfig, minConfig);
            if (minRange != null && res != null) {
                res.append(single.getString().replace(minRange.getString(), ""));
                // res -> "30% - 50% Poison Avoidance"
            }
        }

        if (res != null) {
            return atr.getReader().formatConfigDisplay(LogicalSide.CLIENT, res);
        }
        return null;
    }
}
