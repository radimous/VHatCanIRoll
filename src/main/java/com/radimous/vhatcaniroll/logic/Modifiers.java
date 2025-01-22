package com.radimous.vhatcaniroll.logic;

import com.radimous.vhatcaniroll.Config;
import com.radimous.vhatcaniroll.mixin.AbilityFloatValueAttributeReaderInvoker;
import com.radimous.vhatcaniroll.mixin.EffectConfigAccessor;
import com.radimous.vhatcaniroll.mixin.VaultGearTierConfigAccessor;
import iskallia.vault.config.UniqueGearConfig;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.ability.AbilityAreaOfEffectPercentAttribute;
import iskallia.vault.gear.attribute.ability.AbilityLevelAttribute;
import iskallia.vault.gear.attribute.ability.special.EntropyPoisonModification;
import iskallia.vault.gear.attribute.ability.special.FrostNovaVulnerabilityModification;
import iskallia.vault.gear.attribute.ability.special.base.SpecialAbilityConfig;
import iskallia.vault.gear.attribute.ability.special.base.SpecialAbilityGearAttribute;
import iskallia.vault.gear.attribute.ability.special.base.SpecialAbilityModification;
import iskallia.vault.gear.attribute.ability.special.base.template.config.IntRangeConfig;
import iskallia.vault.gear.attribute.ability.special.base.template.value.IntValue;
import iskallia.vault.gear.attribute.config.BooleanFlagGenerator;
import iskallia.vault.gear.attribute.config.ConfigurableAttributeGenerator;
import iskallia.vault.gear.attribute.custom.effect.EffectGearAttribute;
import iskallia.vault.gear.reader.VaultGearModifierReader;
import iskallia.vault.init.ModConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
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
            modList.addAll(getAffixGroupComponents(lvl, affixTagGroup, modifierGroup.get(affixTagGroup), modifierCategory));
        }

        return modList;
    }

    public static List<Component> getUniqueModifierList(int lvl, ModifierCategory modifierCategory, Map<UniqueGearConfig.AffixTargetType, List<ResourceLocation>> modifierIdentifiers) {
        ArrayList<Component> modList = new ArrayList<>();

        for (Map.Entry<UniqueGearConfig.AffixTargetType, List<ResourceLocation>> modifierIdentifier : modifierIdentifiers.entrySet()) {
            modList.addAll(getUniqueAffixComponents(lvl, modifierIdentifier, modifierCategory));
        }

        return modList;
    }

    public static List<Component> getAffixGroupComponents(int lvl, VaultGearTierConfig.ModifierAffixTagGroup affixTagGroup,
                                                          VaultGearTierConfig.AttributeGroup modifierGroups,
                                                          ModifierCategory modifierCategory) {

        ArrayList<Component> componentList = new ArrayList<>();
        if (!Config.SHOW_ABILITY_ENHANCEMENTS.get() && affixTagGroup.equals(VaultGearTierConfig.ModifierAffixTagGroup.ABILITY_ENHANCEMENT)) {
            return componentList;
        }


        Map<String, Integer> groupCounts = countGroups(lvl, modifierGroups, modifierCategory);

        AtomicBoolean noWeightAttr = new AtomicBoolean(false);
        int totalWeight = modifierGroups.stream()
            .mapToInt(modTierGroup -> getModifierTiers(lvl, modTierGroup, modifierCategory).stream().mapToInt(
                    tier -> {
                        if ((affixTagGroup == VaultGearTierConfig.ModifierAffixTagGroup.IMPLICIT
                            || affixTagGroup == VaultGearTierConfig.ModifierAffixTagGroup.BASE_ATTRIBUTES) && groupCounts.get(modTierGroup.getModifierGroup()) == 1) {
                            noWeightAttr.set(true);
                            return 0;
                        }
                        return tier.getWeight();
                    }
                )
                .sum())
            .sum();


        if (totalWeight == 0 && !noWeightAttr.get()) {
            return componentList;
        }

        componentList.add(new TextComponent(affixTagGroup.toString().replace("_", " ")).withStyle(ChatFormatting.BOLD));

        if (Config.SHOW_WEIGHT.get() && shouldShowWeight(modifierCategory, affixTagGroup) && totalWeight > 0) {
            componentList.add(new TextComponent("Total Weight: " + totalWeight).withStyle(ChatFormatting.BOLD));
        }

        Map<String, List<Component>> groupedModifiers = new HashMap<>();
        for (VaultGearTierConfig.ModifierTierGroup modifierTierGroup :modifierGroups) {
            ArrayList<VaultGearTierConfig.ModifierTier<?>> mTierList;

            mTierList = getModifierTiers(lvl, modifierTierGroup, modifierCategory);
            if (mTierList.isEmpty()) {
                continue;
            }
            String modGr = modifierTierGroup.getModifierGroup();


            MutableComponent modComp = getModifierComponent(VaultGearAttributeRegistry.getAttribute(modifierTierGroup.getAttribute()),mTierList);

            if (!(
                (affixTagGroup == VaultGearTierConfig.ModifierAffixTagGroup.BASE_ATTRIBUTES
                    || affixTagGroup == VaultGearTierConfig.ModifierAffixTagGroup.IMPLICIT
                )
                    && groupCounts.get(modGr) == 1)){
                int weight = modTierListWeight(mTierList);
                if (Config.SHOW_WEIGHT.get() && shouldShowWeight(modifierCategory, affixTagGroup)) {
                    modComp.append(new TextComponent(" w"+weight).withStyle(ChatFormatting.GRAY));
                }

                if (Config.SHOW_CHANCE.get() && shouldShowWeight(modifierCategory, affixTagGroup) && totalWeight > 0) {
                    modComp.append(new TextComponent(String.format(" %.2f%%", ((double) weight * 100 / totalWeight))).withStyle(ChatFormatting.GRAY));
                }
            }


            if (groupCounts.get(modGr) > 1) {
                groupedModifiers.computeIfAbsent(modGr, k -> new ArrayList<>()).add(modComp);
                continue;
            }

            MutableComponent full = new TextComponent("  ");

            full.append(modComp);

            componentList.add(full);
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
            mTierList = getModifierTiers(lvl, modifierTierGroup, modifierCategory);
            if (mTierList.isEmpty()) {
                continue;
            }
            MutableComponent modComp = getModifierComponent(VaultGearAttributeRegistry.getAttribute(modifierTierGroup.getAttribute()),mTierList);
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

    private static Map<String, Integer> countGroups(int lvl, VaultGearTierConfig.AttributeGroup modifierTierGroups, ModifierCategory modifierCategory) {
        Map<String, Integer> groupCounts = new HashMap<>();
        for (VaultGearTierConfig.ModifierTierGroup modifierTierGroup : modifierTierGroups) {
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

    public static ArrayList<VaultGearTierConfig.ModifierTier<?>> getModifierTiers(int lvl, VaultGearTierConfig.ModifierTierGroup modifierTierGroup, ModifierCategory modifierCategory) {

        if (modifierCategory == ModifierCategory.NORMAL) {
            return getNormalModifierTiers(lvl, modifierTierGroup);
        }

        var res = new ArrayList<VaultGearTierConfig.ModifierTier<?>>();
        var highest = modifierTierGroup.getHighestForLevel(lvl);
        if (highest == null) {
            return res; // empty
        }
        if (modifierTierGroup.getTags().contains("noLegendary")){
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
    public static <T, C> MutableComponent getModifierComponent(VaultGearAttribute<T> atr,
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

        if (minConfig instanceof SpecialAbilityGearAttribute.SpecialAbilityTierConfig<?,?,?> minConfigSpecial) {
            return getSpecialAbilityAttributeComponent(modifierTiers, minConfigSpecial);
        }

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

            if (minConfig instanceof AbilityAreaOfEffectPercentAttribute.Config minConfigA) {
                return getAbilityAoePercentageComponent(atr, minConfigA, minConfigA);
            }
            return res;
        }
        return new TextComponent("ERR - NULL DISPLAY " + atrName);
    }

    @SuppressWarnings("unchecked") // this thing is insane
    private static @NotNull MutableComponent getSpecialAbilityAttributeComponent(
        ArrayList<VaultGearTierConfig.ModifierTier<?>> modifierTiers,
        SpecialAbilityGearAttribute.SpecialAbilityTierConfig<?, ?, ?> minConfigSpecial) {
        var modification = minConfigSpecial.getModification();
        if (modification instanceof FrostNovaVulnerabilityModification frostNovaVulnerabilityModification) {
            var minToMaxComponent = new TextComponent("<TODO>");
            return (new TextComponent("Frost Nova also applies Level ").append(minToMaxComponent)
                .append(" Vulnerability"));
        }
        if (modification instanceof EntropyPoisonModification entropyPoisonModification) {
            var tiers = (List<SpecialAbilityGearAttribute.SpecialAbilityTierConfig<SpecialAbilityModification<IntRangeConfig, IntValue>, IntRangeConfig, IntValue>>) modifierTiers.stream().map(x -> x.getModifierConfiguration()).toList();
            var minValue = entropyPoisonModification.getMinimumValue(tiers);
            var minValueDisplay = new TextComponent(minValue.map(x -> String.valueOf(x.getValue().getValue())).orElse("NULL"));
            var maxValue = entropyPoisonModification.getMaximumValue(tiers);
            var maxValueDisplay = new TextComponent(maxValue.map(x -> String.valueOf(x.getValue().getValue())).orElse("NULL"));
            MutableComponent cmp = new TextComponent("Entropic Bind also applies Poison ");
            var range = new TextComponent(minValueDisplay.getString() + "-" + maxValueDisplay.getString());
            cmp.append(range);
            return cmp;
        }
        var abilityKey = minConfigSpecial.getAbilityKey();
        return ModConfigs.ABILITIES.getAbilityById(abilityKey).filter(skill -> skill.getName() != null).map(skill -> {
            String name = skill.getName();
            return new TextComponent("Special " + name + " modification");
        }).orElseGet(() -> (TextComponent) new TextComponent(abilityKey).withStyle(Style.EMPTY.withColor(14076214)));
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

        if ((minConfig instanceof AbilityAreaOfEffectPercentAttribute.Config minConfigA) && (maxConfig instanceof AbilityAreaOfEffectPercentAttribute.Config maxConfigA)) {
            return getAbilityAoePercentageComponent(atr, minConfigA, maxConfigA);
        }

        if (minConfig instanceof EffectGearAttribute.Config minEffectConfig
            && maxConfig instanceof EffectGearAttribute.Config
            && maxConfigDisplay != null) {
            var effectStr = ((EffectConfigAccessor)minEffectConfig).getAmplifier() + "-" +
                maxConfigDisplay.getString();
            return new TextComponent(effectStr).withStyle(atr.getReader().getColoredTextStyle());
        }

        if (atrName.equals("the_vault:effect_cloud")){
            return new TextComponent("Special ability modification");
        }

        if (res != null) {
            return atr.getReader().formatConfigDisplay(LogicalSide.CLIENT, res);
        }
        return res;
    }

    private static <T> @NotNull MutableComponent getAbilityAoePercentageComponent(VaultGearAttribute<T> atr,
                                                                        AbilityAreaOfEffectPercentAttribute.Config minConfigA,
                                                                        AbilityAreaOfEffectPercentAttribute.Config maxConfigA) {
        float min = minConfigA.getMin();
        float max = maxConfigA.generateMaximumValue();

        VaultGearModifierReader<T> reader = atr.getReader();
        MutableComponent minValueDisplay = new TextComponent(new DecimalFormat("0.#").format(Math.abs(min * 100.0F)) + "%");
        MutableComponent maxValueDisplay = new TextComponent(new DecimalFormat("0.#").format(Math.abs(max * 100.0F)) + "%");
        boolean positive = min > 0;
        MutableComponent areaCmp = new TextComponent("Area Of Effect").withStyle(Style.EMPTY.withColor(ModConfigs.COLORS.getColor("areaOfEffect")));
        String cdInfo;
        if (positive) {
            cdInfo = " more ";
        } else {
            cdInfo = " less ";
        }

        return new TextComponent("")
            .append(VaultGearModifier.AffixType.IMPLICIT.getAffixPrefixComponent(true)
                .withStyle(Style.EMPTY.withColor(6082075)))
            .append(minValueDisplay.withStyle(Style.EMPTY.withColor(6082075)))
            .append(new TextComponent("-").withStyle(Style.EMPTY.withColor(6082075)))
            .append(maxValueDisplay.withStyle(Style.EMPTY.withColor(6082075)))
            .append(cdInfo)
            .append(areaCmp)
            .append(" of ")
            .append(
                ((AbilityFloatValueAttributeReaderInvoker) reader).invokeFormatAbilityName(minConfigA.getAbilityKey()))
            .setStyle(reader.getColoredTextStyle());
    }

    private static MutableComponent getCloudRangeComponent(MutableComponent minConfigDisplay, MutableComponent maxConfigDisplay, VaultGearAttribute<?> atr) {
        // <Effect> [<LVL>] Cloud [when Hit]
        // Poison Cloud
        // Poison III Cloud
        var minString = minConfigDisplay.getString();
        var maxString = maxConfigDisplay.getString();

        var minLvl = getCloudLvl(minString);
        var maxLvl = getCloudLvl(maxString);

        if (minLvl.equals(maxLvl)) {
            return minConfigDisplay.withStyle(atr.getReader().getColoredTextStyle());
        }

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

    private static MutableComponent abilityLvlComponent(MutableComponent prev, VaultGearAttribute<?> atr,
                                                 AbilityLevelAttribute.Config minConfig) {

        var abComp = new TextComponent("+").withStyle(atr.getReader().getColoredTextStyle());
        var optSkill = ModConfigs.ABILITIES.getAbilityById(minConfig.getAbilityKey());
        if (optSkill.isEmpty()) {
            return prev.append(" added ability levels").withStyle(atr.getReader().getColoredTextStyle());
        }
        var abName = optSkill.get().getName();
        var parts = prev.getString().split("-");

        var res = new TextComponent("").withStyle(prev.getStyle());
        if (parts.length == 2) {
            if (parts[0].equals(parts[1])) {
                res.append(parts[0]);
            } else {
                res.append(parts[0]);
                res.append("-");
                res.append(parts[1]);
            }
        } else {
            res.append(prev);
        }
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

    private static boolean shouldShowWeight(ModifierCategory modifierCategory, VaultGearTierConfig.ModifierAffixTagGroup affixTagGroup) {
        return modifierCategory == ModifierCategory.NORMAL && !Config.AFFIX_TAG_GROUP_CHANCE_BLACKLIST.get().contains(affixTagGroup.name());
    }
}
