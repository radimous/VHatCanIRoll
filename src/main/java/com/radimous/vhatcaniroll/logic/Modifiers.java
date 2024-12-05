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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is responsible for all the logic of transforming vh config -> list of components needed for the UI
 */
public class Modifiers {
    private static final ChatFormatting[] COLORS =
        new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.GREEN, ChatFormatting.BLUE, ChatFormatting.YELLOW,
            ChatFormatting.LIGHT_PURPLE, ChatFormatting.AQUA, ChatFormatting.WHITE};

    private static final Pattern CLOUD_PATTERN = Pattern.compile("^(?<effect>.*?) ?(?<lvl>I|II|III|IV|V|VI|VII|VIII|IX|X)? (?<suffix>Cloud.*)$");

    public static List<Component> getModifierList(int lvl, VaultGearTierConfig cfg, ModifierCategory modifierCategory) {
        Map<VaultGearTierConfig.ModifierAffixTagGroup, VaultGearTierConfig.AttributeGroup> modifierGroup = ((VaultGearTierConfigAccessor) cfg).getModifierGroup();
        
        ArrayList<Component> modList = new ArrayList<>();

        for (VaultGearTierConfig.ModifierAffixTagGroup affixTagGroup : modifierGroup.keySet()) {
            modList.addAll(getAffixGroupComponents(lvl, affixTagGroup, modifierGroup, modifierCategory));
        }

        return modList;
    }

    private static List<Component> getAffixGroupComponents(int lvl, VaultGearTierConfig.ModifierAffixTagGroup affixTagGroup,
                                             Map<VaultGearTierConfig.ModifierAffixTagGroup, VaultGearTierConfig.AttributeGroup> modifierGroup,
                                             ModifierCategory modifierCategory) {

        ArrayList<Component> componentList = new ArrayList<>();
        if (!Config.SHOW_ABILITY_ENHANCEMENTS.get() && affixTagGroup.equals(VaultGearTierConfig.ModifierAffixTagGroup.ABILITY_ENHANCEMENT)) {
            return componentList;
        }


        int totalWeight = modifierGroup.get(affixTagGroup).stream()
            .mapToInt(x -> getModifierTiers(lvl, x, modifierCategory).stream().mapToInt(VaultGearTierConfig.ModifierTier::getWeight).sum())
            .sum();

        if (totalWeight == 0) {
            return componentList;
        }

        componentList.add(new TextComponent(affixTagGroup.toString().replace("_", " ")).withStyle(ChatFormatting.BOLD));

        if (Config.SHOW_WEIGHT.get() && modifierCategory == ModifierCategory.NORMAL && affixTagGroup != VaultGearTierConfig.ModifierAffixTagGroup.BASE_ATTRIBUTES) {
            componentList.add(new TextComponent("Total Weight: " + totalWeight).withStyle(ChatFormatting.BOLD));
        }


        Map<String, Integer> groupCounts = countGroups(lvl, affixTagGroup, modifierGroup, modifierCategory);

        Map<String, List<Component>> groupedModifiers = new HashMap<>();
        for (VaultGearTierConfig.ModifierTierGroup modifierTierGroup : modifierGroup.get(affixTagGroup)) {
            ArrayList<VaultGearTierConfig.ModifierTier<?>> mTierList;

            mTierList = getModifierTiers(lvl, modifierTierGroup, modifierCategory);
            if (mTierList.isEmpty()) {
                continue;
            }
            String modGr = modifierTierGroup.getModifierGroup();
            
 
            MutableComponent modComp = getModifierComponent(VaultGearAttributeRegistry.getAttribute(modifierTierGroup.getAttribute()),mTierList);

            int weight = modTierListWeight(mTierList);
            if (Config.SHOW_WEIGHT.get() && modifierCategory == ModifierCategory.NORMAL && affixTagGroup != VaultGearTierConfig.ModifierAffixTagGroup.BASE_ATTRIBUTES) {
                modComp.append(new TextComponent(" w"+weight).withStyle(ChatFormatting.GRAY));
            }

            if (Config.SHOW_CHANCE.get() && modifierCategory == ModifierCategory.NORMAL && affixTagGroup != VaultGearTierConfig.ModifierAffixTagGroup.BASE_ATTRIBUTES) {
                modComp.append(new TextComponent(String.format(" %.2f%%", ((double) weight * 100 / totalWeight))).withStyle(ChatFormatting.GRAY));
            }

            if (groupCounts.get(modGr) > 1) {
                groupedModifiers.computeIfAbsent(modGr, k -> new ArrayList<>()).add(modComp);
                continue;
            }

            MutableComponent full = new TextComponent("  ");

            full.append(modComp);

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
                                                    ModifierCategory modifierCategory) {
        Map<String, Integer> groupCounts = new HashMap<>();
        for (VaultGearTierConfig.ModifierTierGroup modifierTierGroup : modifierGroup.get(affixTagGroup)) {
            ArrayList<VaultGearTierConfig.ModifierTier<?>> mTierList;
            mTierList = getModifierTiers(lvl, modifierTierGroup, modifierCategory);
            if (mTierList.isEmpty()) {
                continue;
            }
            groupCounts.put(modifierTierGroup.getModifierGroup(),
                groupCounts.getOrDefault(modifierTierGroup.getModifierGroup(), 0) + 1);
        }
        return groupCounts;
    }

    //TODO: check how noLegendary works in VH
    private static ArrayList<VaultGearTierConfig.ModifierTier<?>> getModifierTiers(int lvl,
                                                                                   VaultGearTierConfig.ModifierTierGroup modifierTierGroup, ModifierCategory modifierCategory) {

        if (modifierCategory == ModifierCategory.NORMAL) {
            return getNormalModifierTiers(lvl, modifierTierGroup);
        }

        var res = new ArrayList<VaultGearTierConfig.ModifierTier<?>>();
        var highest = modifierTierGroup.getHighestForLevel(lvl);
        if (highest == null) {
            return res; // empty
        }
        int index = Math.min(highest.getModifierTier() + modifierCategory.getTierIncrease(), modifierTierGroup.size() - 1);
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
    @NotNull private static ArrayList<VaultGearTierConfig.ModifierTier<?>> getNormalModifierTiers(int lvl,
                                                                                                  VaultGearTierConfig.ModifierTierGroup modifierTierGroup) {
        return modifierTierGroup.getModifiersForLevel(lvl).stream()
            .filter(x -> x.getWeight() != 0
                && !(x.getModifierConfiguration() instanceof BooleanFlagGenerator.BooleanFlag bf &&
                !bf.get())) // bool with false :( looking at you, soulbound
            .collect(Collectors.toCollection(ArrayList::new));
    }

    @SuppressWarnings("unchecked") // I don't think proper generics are possible, VaultGearTierConfig#getModifiersForLevel returns List<ModifierTier<?>>
    private static <T, C> MutableComponent getModifierComponent(VaultGearAttribute<T> atr,
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
            if (res != null) {
                return res;
            }
        }
        if (minConfigDisplay != null) {
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
            
            //FIXME: clouds with roman numerals are not working
            if ((atrName.equals("the_vault:effect_avoidance") || atrName.equals("the_vault:effect_list_avoidance")) && minConfigDisplay != null) {
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
            if (minConfigDisplay != null && maxConfigDisplay != null && (atrName.equals("the_vault:effect_cloud") || atrName.equals("the_vault:effect_cloud_when_hit"))) {
               return getCloudRangeComponent(minConfigDisplay, maxConfigDisplay, atr);
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

    private static MutableComponent getCloudRangeComponent(MutableComponent minConfigDisplay, MutableComponent maxConfigDisplay, VaultGearAttribute<?> atr) {
        // <Effect> [<LVL>] Cloud [when Hit]
        // Poison Cloud
        // Poison III Cloud
        var minString = minConfigDisplay.getString();
        var maxString = maxConfigDisplay.getString();

        var minLvl = getCloudLvl(minString);
        var maxLvl = getCloudLvl(maxString);

        var cloudRange = makeCloudLvlRange(minString, minLvl, maxLvl);
        return new TextComponent(cloudRange).withStyle(atr.getReader().getColoredTextStyle());
    }

    private static String getCloudLvl(String displayString){
        var matcher = CLOUD_PATTERN.matcher(displayString);
        if (matcher.find()) {
            if (matcher.group("lvl") != null) {
                return matcher.group("lvl");
            }
            return "I";
        }
        return "I";
    }

    private static String makeCloudLvlRange(String displayString, String minLvl, String maxLvl){
        var matcher = CLOUD_PATTERN.matcher(displayString);
        if (matcher.find()) {
            return matcher.group("effect") + " " + minLvl + "-" + maxLvl + " " + matcher.group("suffix");
        }
        return displayString;
    }

    private static MutableComponent abilityLvlComponent(MutableComponent res, VaultGearAttribute<?> atr,
                                                 AbilityLevelAttribute.Config minConfig) {

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
