package com.radimous.vhatcaniroll.logic;

import com.radimous.vhatcaniroll.Config;
import com.radimous.vhatcaniroll.mixin.EffectConfigAccessor;
import com.radimous.vhatcaniroll.mixin.VaultGearTierConfigAccessor;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.attribute.ability.AbilityLevelAttribute;
import iskallia.vault.gear.attribute.config.BooleanFlagGenerator;
import iskallia.vault.gear.attribute.config.ConfigurableAttributeGenerator;
import iskallia.vault.gear.attribute.custom.effect.EffectGearAttribute;
import iskallia.vault.init.ModConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is responsible for all the logic of transforming vh config -> list of components needed for the UI
 */
public class Modifiers {
    private static final ChatFormatting[] COLORS =
        new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.GREEN, ChatFormatting.BLUE, ChatFormatting.YELLOW,
            ChatFormatting.LIGHT_PURPLE, ChatFormatting.AQUA, ChatFormatting.WHITE};

    public static List<Component> getModifierList(int lvl, VaultGearTierConfig cfg, int tierIncrease) {
        Map<VaultGearTierConfig.ModifierAffixTagGroup, VaultGearTierConfig.AttributeGroup> modifierGroup = ((VaultGearTierConfigAccessor) cfg).getModifierGroup();
        
        ArrayList<Component> modList = new ArrayList<>();

        for (VaultGearTierConfig.ModifierAffixTagGroup affixTagGroup : modifierGroup.keySet()) {
            modList.addAll(getAffixGroupComponents(lvl, affixTagGroup, modifierGroup, tierIncrease));
        }

        return modList;
    }

    private static List<Component> getAffixGroupComponents(int lvl, VaultGearTierConfig.ModifierAffixTagGroup affixTagGroup,
                                             Map<VaultGearTierConfig.ModifierAffixTagGroup, VaultGearTierConfig.AttributeGroup> modifierGroup,
                                             int tierIncrease) {

        ArrayList<Component> componentList = new ArrayList<>();
        if (!Config.SHOW_ABILITY_ENHANCEMENTS.get() && affixTagGroup.equals(VaultGearTierConfig.ModifierAffixTagGroup.ABILITY_ENHANCEMENT)) {
            return componentList;
        }
        if (modifierGroup.get(affixTagGroup).isEmpty()) {
            return componentList;
        }
        componentList.add(new TextComponent(affixTagGroup.toString().replace("_", " ")).withStyle(ChatFormatting.BOLD));

        int totalWeight = modifierGroup.get(affixTagGroup).stream()
            .mapToInt(x -> getModifierTiers(lvl, x).stream().mapToInt(VaultGearTierConfig.ModifierTier::getWeight).sum())
            .sum();
        if (Config.SHOW_WEIGHT.get()) {
            componentList.add(new TextComponent("Total Weight: " + totalWeight).withStyle(ChatFormatting.BOLD));
        }


        Map<String, Integer> groupCounts = countGroups(lvl, affixTagGroup, modifierGroup, tierIncrease);

        Map<String, List<Component>> groupedModifiers = new HashMap<>();
        for (VaultGearTierConfig.ModifierTierGroup modifierTierGroup : modifierGroup.get(affixTagGroup)) {
            ArrayList<VaultGearTierConfig.ModifierTier<?>> mTierList;

            // TODO: support greater modifiers (greater is +1 tier, legendary is +2 tiers) (look how VH does it)
            // maybe ENUM - NORMAL, GREATER, LEGENDARY and the button would cycle through them
            if (tierIncrease > 0) {
                mTierList = getIncreasedModifierTiers(lvl, modifierTierGroup, tierIncrease);
            } else {
                mTierList = getModifierTiers(lvl, modifierTierGroup);
            }

            if (mTierList.isEmpty()) {
                continue;
            }
            String modGr = modifierTierGroup.getModifierGroup();
            
 
            Component newMod = getModifierComponent(VaultGearAttributeRegistry.getAttribute(modifierTierGroup.getAttribute()),mTierList);
            if (groupCounts.get(modGr) > 1) {
                groupedModifiers.computeIfAbsent(modGr, k -> new ArrayList<>()).add(newMod);
                continue;
            }

            MutableComponent full = new TextComponent("  ");

            full.append(newMod);

            int weight = modTierListWeight(mTierList);
            if (Config.SHOW_WEIGHT.get()) {
                full.append(" w"+weight);
            }

            if (Config.SHOW_CHANCE.get()) {
                full.append(String.format(" %.2f %%", ((double) weight * 100 / totalWeight)));
            }

            if (Config.ALLOW_DUPE.get() || !(componentList.get(componentList.size() - 1).getString()).equals(full.getString())) { //dumb way to fix ability lvl+ duplication
                componentList.add(full);
            }
        }

        // more than 7 groups is a bit crazy, but just in case
        boolean useNums = groupedModifiers.size() > COLORS.length;
        int i = 0;
        for (var modGr: groupedModifiers.values()) {
           for (var mod: modGr) {
               MutableComponent full = new TextComponent(useNums ? i + " " : "► ").withStyle(COLORS[i % COLORS.length]);
               full.append(mod);
               componentList.add(full);
           }
           i++;
        }
        componentList.add(TextComponent.EMPTY);
        return componentList;
    }

    private static Map<String, Integer> countGroups(int lvl, VaultGearTierConfig.ModifierAffixTagGroup affixTagGroup,
                                                    Map<VaultGearTierConfig.ModifierAffixTagGroup, VaultGearTierConfig.AttributeGroup> modifierGroup,
                                                    int tierIncrease) {
        Map<String, Integer> groupCounts = new HashMap<>();
        for (VaultGearTierConfig.ModifierTierGroup modifierTierGroup : modifierGroup.get(affixTagGroup)) {
            ArrayList<VaultGearTierConfig.ModifierTier<?>> mTierList;
            if (tierIncrease > 0) {
                mTierList = getIncreasedModifierTiers(lvl, modifierTierGroup, tierIncrease);
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

    //TODO: check how noLegendary works in VH
    private static ArrayList<VaultGearTierConfig.ModifierTier<?>> getIncreasedModifierTiers(int lvl,
                                                                                VaultGearTierConfig.ModifierTierGroup modifierTierGroup, int tierIncrease) {

        var res = new ArrayList<VaultGearTierConfig.ModifierTier<?>>();
        var highest = modifierTierGroup.getHighestForLevel(lvl);
        if (highest == null) {
            return res; // empty
        }
        int index = Math.min(highest.getModifierTier() + tierIncrease, modifierTierGroup.size() - 1);
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

    @SuppressWarnings("unchecked") // I don't think proper generics are possible, VaultGearTierConfig#getModifiersForLevel returns List<ModifierTier<?>>
    private static <T, C> Component getModifierComponent(VaultGearAttribute<T> atr,
                                           ArrayList<VaultGearTierConfig.ModifierTier<?>> modifierTiers) {
        if (modifierTiers.isEmpty()) {
            return new TextComponent("ERR - EMPTY MODIFIER TIERS");
        }

        if (atr == null) {
            return new TextComponent("ERR - NULL ATTRIBUTE");
        }

        ConfigurableAttributeGenerator<T, C> atrGenerator = (ConfigurableAttributeGenerator<T, C>) atr.getGenerator();
        if (atrGenerator == null) {
            return new TextComponent("ERR - NULL ATTRIBUTE GENERATOR");
        }
        C minConfig = (C) modifierTiers.get(0).getModifierConfiguration();
        C maxConfig = (C) modifierTiers.get(modifierTiers.size() - 1).getModifierConfiguration();
        ResourceLocation atrRegName = atr.getRegistryName();
        if (atrRegName == null) {
            return new TextComponent("ERR - NULL REGISTRY NAME");
        }
        String atrName = atrRegName.toString();

        var minConfigDisplay = atrGenerator.getConfigDisplay(atr.getReader(), minConfig);
        
        MutableComponent res = null;
        if (modifierTiers.size() > 1) {
            res = rangeComponent(atrName, atr, atrGenerator, minConfig, maxConfig);
        }
        if (res == null && minConfigDisplay != null) {
            res = minConfigDisplay.withStyle(atr.getReader().getColoredTextStyle());
            if (minConfig instanceof AbilityLevelAttribute.Config minConfigAbility) {
                return abilityLvlComponent(res, atr, minConfigAbility);
            }
           
            if (minConfig instanceof EffectGearAttribute.Config ) {
                return minConfigDisplay;
            }
            return res;
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
            var minConfigDisplay = atrGenerator.getConfigDisplay(atr.getReader(), minConfig);
            var maxConfigDisplay = atrGenerator.getConfigDisplay(atr.getReader(), maxConfig);


            if (res != null && minConfig instanceof AbilityLevelAttribute.Config minConfigAbility) {
                return abilityLvlComponent(res, atr, minConfigAbility);
            }
            
            //FIXME: poison avoidance was changed to single generic "Effect Avoidance" and it's not working
            //FIXME: clouds with roman numerals are not working
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
            return res;
    }

    private static MutableComponent abilityLvlComponent(MutableComponent res, VaultGearAttribute<?> atr,
                                                 AbilityLevelAttribute.Config minConfig) {

        if (Config.COMBINE_LVL_TO_ABILITIES.get()) {
            return res.append(" added ability levels").withStyle(atr.getReader().getColoredTextStyle());
        }

        var abComp = new TextComponent("+").withStyle(atr.getReader().getColoredTextStyle());
        var optSkill = ModConfigs.ABILITIES.getAbilityById(minConfig.getAbilityKey());
        if (optSkill.isEmpty()) {
            return res.append(" added ability levels").withStyle(atr.getReader().getColoredTextStyle());
        }
        var abName = optSkill.get().getName();
        abComp.append(res);
        abComp.append(" to level of ");
        abComp.append(new TextComponent(abName).withStyle(Style.EMPTY.withColor(14076214)));
        return abComp;
    }
    
    private static int modTierListWeight(List<VaultGearTierConfig.ModifierTier<?>> val) {
        if (val == null || val.isEmpty()) {
            return 0;
        }
        return val.stream().mapToInt(VaultGearTierConfig.ModifierTier::getWeight).sum();
    }
}