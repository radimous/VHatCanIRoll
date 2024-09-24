package com.radimous.vhatcaniroll;

import com.radimous.vhatcaniroll.mixin.EffectConfigAccessor;
import com.radimous.vhatcaniroll.mixin.VaultGearTierConfigAccessor;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.attribute.ability.AbilityLevelAttribute;
import iskallia.vault.gear.attribute.ability.special.base.SpecialAbilityModification;
import iskallia.vault.gear.attribute.config.BooleanFlagGenerator;
import iskallia.vault.gear.attribute.config.ConfigurableAttributeGenerator;
import iskallia.vault.gear.attribute.custom.EffectGearAttribute;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.util.TextComponentUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Helper {
    private static final ChatFormatting[] COLORS =
        new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.GREEN, ChatFormatting.BLUE, ChatFormatting.YELLOW,
            ChatFormatting.LIGHT_PURPLE, ChatFormatting.AQUA, ChatFormatting.WHITE};

    public static List<Component> getModifierList(int lvl, VaultGearTierConfig cfg, boolean legendary) {
        Map<VaultGearTierConfig.ModifierAffixTagGroup, VaultGearTierConfig.AttributeGroup> modifierGroup =
            ((VaultGearTierConfigAccessor) cfg).getModifierGroup();
        ArrayList<Component> modList = new ArrayList<>();
        for (VaultGearTierConfig.ModifierAffixTagGroup affixTagGroup : modifierGroup.keySet()) {
            processAffixTagGroup(lvl, affixTagGroup, modifierGroup, modList, legendary);
        }
        return modList;
    }

    private static void processAffixTagGroup(int lvl, VaultGearTierConfig.ModifierAffixTagGroup affixTagGroup,
                                             Map<VaultGearTierConfig.ModifierAffixTagGroup, VaultGearTierConfig.AttributeGroup> modifierGroup,
                                             ArrayList<Component> modList, boolean legendary) {
        if (affixTagGroup.equals(VaultGearTierConfig.ModifierAffixTagGroup.ABILITY_ENHANCEMENT)) {
            return;
        }
        if (modifierGroup.get(affixTagGroup).isEmpty()) {
            return;
        }
        modList.add(new TextComponent(affixTagGroup.toString().replace("_", " ")).withStyle(ChatFormatting.BOLD));

        Map<String, Integer> groupCounts = countGroups(lvl, affixTagGroup, modifierGroup, legendary);

        List<String> grList = new ArrayList<>();
        for (VaultGearTierConfig.ModifierTierGroup modifierTierGroup : modifierGroup.get(affixTagGroup)) {
            ArrayList<VaultGearTierConfig.ModifierTier<?>> mTierList;
            if (legendary) {
                mTierList = getLegendaryModifierTiers(lvl, modifierTierGroup);
            } else {
                mTierList = getModifierTiers(lvl, modifierTierGroup);
            }

            if (mTierList.isEmpty()) {
                continue;
            }
            String modGr = modifierTierGroup.getModifierGroup();
            Component newMod = getVal(
                Objects.requireNonNull(VaultGearAttributeRegistry.getAttribute(modifierTierGroup.getAttribute())),
                mTierList);
            if (groupCounts.get(modGr) > 1 && !grList.contains(modGr)) {
                grList.add(modGr);
            }
            int index = grList.indexOf(modGr); // index used to determine color
            MutableComponent full;
            if (index == -1) {
                full = new TextComponent("  ");
            } else {
                full = new TextComponent("► ").withStyle(COLORS[index % COLORS.length]);
            }

            full.append(newMod);
            if (Config.ALLOW_DUPE.get() || !(modList.get(modList.size() - 1).getString()).equals(full.getString())) { //dumb way to fix ability lvl+ duplication
                modList.add(full);
            }
        }
        modList.add(TextComponent.EMPTY);
    }

    private static Map<String, Integer> countGroups(int lvl, VaultGearTierConfig.ModifierAffixTagGroup affixTagGroup,
                                                    Map<VaultGearTierConfig.ModifierAffixTagGroup, VaultGearTierConfig.AttributeGroup> modifierGroup,
                                                    boolean legendary) {
        Map<String, Integer> groupCounts = new HashMap<>();
        for (VaultGearTierConfig.ModifierTierGroup modifierTierGroup : modifierGroup.get(affixTagGroup)) {
            ArrayList<VaultGearTierConfig.ModifierTier<?>> mTierList;
            if (legendary) {
                mTierList = getLegendaryModifierTiers(lvl, modifierTierGroup);
            } else {
                mTierList = getModifierTiers(lvl, modifierTierGroup);
            }
            if (mTierList.isEmpty()) {
                continue;
            }
            groupCounts.put(modifierTierGroup.getModifierGroup(),
                groupCounts.getOrDefault(modifierTierGroup.getModifierGroup(), 0) + 1);
        }
        return groupCounts;
    }

    private static ArrayList<VaultGearTierConfig.ModifierTier<?>> getLegendaryModifierTiers(int lvl,
                                                                                 VaultGearTierConfig.ModifierTierGroup modifierTierGroup) {

        var res = new ArrayList<VaultGearTierConfig.ModifierTier<?>>();
        var highest = modifierTierGroup.getHighestForLevel(lvl);
        if (highest == null) {
            return res; // empty
        }
        int index = Math.min(highest.getModifierTier() + 2, modifierTierGroup.size() - 1);
        var legendTier = modifierTierGroup.get(index);
        if (legendTier == null || legendTier.getWeight() == 0){
            return res; // empty
        }
        if (legendTier.getModifierConfiguration() instanceof BooleanFlagGenerator.BooleanFlag bf &&
            !bf.get()) {
            return res; // empty
        }
        res.add(legendTier);
        return res; // only one
    }
    @NotNull private static ArrayList<VaultGearTierConfig.ModifierTier<?>> getModifierTiers(int lvl,
                                                                                            VaultGearTierConfig.ModifierTierGroup modifierTierGroup) {
        return modifierTierGroup.getModifiersForLevel(lvl).stream()
            .filter(x -> x.getWeight() != 0
                && !(x.getModifierConfiguration() instanceof BooleanFlagGenerator.BooleanFlag bf &&
                !bf.get())) // bool with false :( looking at you, soulbound
            .collect(Collectors.toCollection(ArrayList::new));
    }

    // TODO: wtf is this, fix variable names and make it readable
    // I don't think proper generics are possible, VaultGearTierConfig#getModifiersForLevel returns List<ModifierTier<?>>
    private static <T, C> Component getVal(VaultGearAttribute<T> atr,
                                           ArrayList<VaultGearTierConfig.ModifierTier<?>> val) {
        if (val.isEmpty()) {
            return new TextComponent("ERR - EMPTY VAL");
        }
        ConfigurableAttributeGenerator<T, C> atrGenerator = (ConfigurableAttributeGenerator<T, C>) atr.getGenerator();
        if (atrGenerator == null) {
            return new TextComponent("ERR - NULL GENERATOR");
        }
        C minConfig = (C) val.get(0).getModifierConfiguration();
        C maxConfig = (C) val.get(val.size() - 1).getModifierConfiguration();
        MutableComponent res;
        ResourceLocation atrRegName = atr.getRegistryName();
        if (atrRegName == null) {
            return new TextComponent("ERR - NULL REGISTRY NAME");
        }
        String atrName = atrRegName.toString();

        var minConfigDisplay = atrGenerator.getConfigDisplay(atr.getReader(), minConfig);
        var maxConfigDisplay = atrGenerator.getConfigDisplay(atr.getReader(), maxConfig);

        if (val.size() > 1) {
            // range
            res = atrGenerator.getConfigRangeDisplay(atr.getReader(), minConfig, maxConfig);

            if (res != null && minConfig instanceof AbilityLevelAttribute.Config minConfigAbility) {
                return abilityLvlComponent(res, atr, minConfigAbility);
            }
            if (res != null && atrName.equals("the_vault:wendarr_affinity")) {
                return res.append(" God Affinity").withStyle(atr.getReader().getColoredTextStyle());
            }
            if (atrName.equals("the_vault:effect_avoidance") && minConfigDisplay != null) {
                // res -> "30% - 50%"
                // single ->  "30% Poison Avoidance"
                // minRange -> "30%"
                var single = minConfigDisplay.withStyle(atr.getReader().getColoredTextStyle());
                var minRange = atrGenerator.getConfigRangeDisplay(atr.getReader(), minConfig, minConfig);
                if (minRange != null && res != null) {
                    res.append(single.getString().replace(minRange.getString(), ""));
                    // res -> "30% - 50% Poison Avoidance"
                }
            }
            if (minConfig instanceof EffectGearAttribute.Config minEffectConfig
                && maxConfig instanceof EffectGearAttribute.Config
                && maxConfigDisplay != null) {
                var effectStr = ((EffectConfigAccessor)minEffectConfig).getAmplifier() + "-" +
                    maxConfigDisplay.getString();
                return new TextComponent(effectStr).withStyle(atr.getReader().getColoredTextStyle());

            }
            if (res != null) {
                return atr.getReader().formatConfigDisplay(LogicalSide.CLIENT, res);
            }
        }
        if (minConfigDisplay != null) {
            res = minConfigDisplay.withStyle(atr.getReader().getColoredTextStyle());
            if (minConfig instanceof AbilityLevelAttribute.Config minConfigAbility) {
                return abilityLvlComponent(res, atr, minConfigAbility);
            }
            if (atrName.equals("the_vault:wendarr_affinity")) {
                return TextComponentUtils.replace(TextComponentUtils.createSourceStack(LogicalSide.CLIENT), res,
                        "Wendarr Affinity", new TextComponent("God Affinity"))
                    .withStyle(atr.getReader().getColoredTextStyle());
            }
            if (minConfig instanceof EffectGearAttribute.Config ) {
                return minConfigDisplay;
            }
            return res;
        }
        return new TextComponent("ERR - NULL DISPLAY " + atrName);
    }
    private static Component abilityLvlComponent(MutableComponent res, VaultGearAttribute<?> atr,
                                                 AbilityLevelAttribute.Config minConfig) {

        if (Config.COMBINE_LVL_TO_ABILITIES.get()) {
            return res.append(" added ability levels").withStyle(atr.getReader().getColoredTextStyle());
        } else {
            var abComp = new TextComponent("+").withStyle(atr.getReader().getColoredTextStyle());
            var optSkill = ModConfigs.ABILITIES.getAbilityById(minConfig.getAbilityKey());
            if (optSkill.isEmpty()) {
                return res.append(" added ability levels").withStyle(atr.getReader().getColoredTextStyle());
            }
            var abName = optSkill.get().getName();
            abComp.append(res);
            abComp.append(" to level of ");
            abComp.append(new TextComponent(abName).withStyle(SpecialAbilityModification.getAbilityStyle()));
            return abComp;
        }
    }
}
