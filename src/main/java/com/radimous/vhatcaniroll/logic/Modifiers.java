package com.radimous.vhatcaniroll.logic;

import com.radimous.vhatcaniroll.Config;
import com.radimous.vhatcaniroll.logic.modifiervalues.ModifierValues;
import com.radimous.vhatcaniroll.mixin.accessors.VaultGearTierConfigAccessor;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.attribute.config.BooleanFlagGenerator;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * This is responsible for all the logic of transforming vh config -> list of components needed for the UI
 */
public class Modifiers {
    private static final ChatFormatting[] COLORS =
        new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.GREEN, ChatFormatting.BLUE, ChatFormatting.YELLOW,
            ChatFormatting.LIGHT_PURPLE, ChatFormatting.AQUA, ChatFormatting.WHITE};

    public static List<Component> getModifierList(int lvl, VaultGearTierConfig cfg, ModifierCategory modifierCategory) {
        Map<VaultGearTierConfig.ModifierAffixTagGroup, VaultGearTierConfig.AttributeGroup> modifierGroup = ((VaultGearTierConfigAccessor) cfg).getModifierGroup();

        ArrayList<Component> modList = new ArrayList<>();

        for (VaultGearTierConfig.ModifierAffixTagGroup affixTagGroup : modifierGroup.keySet()) {
            modList.addAll(getAffixGroupComponents(lvl, affixTagGroup, modifierGroup.get(affixTagGroup), modifierCategory));
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
        Map<ResourceLocation, Pair<Integer, Integer>> boolWeights = new HashMap<>();

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

         for (var modifierTierGroup : modifierGroups){
             for (VaultGearTierConfig.ModifierTier<?> modifierTier : modifierTierGroup.getModifiersForLevel(lvl)) {
                 if (modifierTier.getModifierConfiguration() instanceof BooleanFlagGenerator.BooleanFlag bf){
                     var currBoolWeights = boolWeights.get(modifierTierGroup.getAttribute());
                        if (currBoolWeights == null) {
                            boolWeights.put(modifierTierGroup.getAttribute(), Pair.of(bf.get() ? modifierTier.getWeight() : 0, bf.get() ? 0 : modifierTier.getWeight()));
                        } else {
                            boolWeights.put(modifierTierGroup.getAttribute(),
                                Pair.of(currBoolWeights.left() + (bf.get() ? modifierTier.getWeight() : 0),
                                    currBoolWeights.right() + (bf.get() ? 0 : modifierTier.getWeight())));
                        }
                 }
             }
         }

        var toRemove = new ArrayList<ResourceLocation>();
        for (var entry : boolWeights.entrySet()) {
            if (entry.getValue().left() == 0 || entry.getValue().right() == 0) {
                toRemove.add(entry.getKey());
            }
        }
        for (var entry : toRemove) {
            boolWeights.remove(entry);
        }

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


            MutableComponent modComp = ModifierValues.getModifierComponent(VaultGearAttributeRegistry.getAttribute(modifierTierGroup.getAttribute()),mTierList);

            int weight = modTierListWeight(mTierList);
            if (Config.SHOW_WEIGHT.get() && shouldShowWeight(modifierCategory, affixTagGroup) && totalWeight > 0) {
                modComp.append(new TextComponent(" w"+weight).withStyle(ChatFormatting.GRAY));
            }

            if (Config.SHOW_CHANCE.get() && shouldShowWeight(modifierCategory, affixTagGroup) && totalWeight > 0) {
                modComp.append(new TextComponent(String.format(" %.2f%%", ((double) weight * 100 / totalWeight))).withStyle(ChatFormatting.GRAY));
            }

            if (Config.SHOW_CHANCE.get() && !boolWeights.isEmpty()) {
                Pair<Integer, Integer> boolWeight = boolWeights.get(modifierTierGroup.getAttribute());
                if (boolWeight != null) {
                    modComp.append(new TextComponent(String.format(" %.2f%%", ((double) boolWeight.left() * 100 / (boolWeight.left() + boolWeight.right())))).withStyle(ChatFormatting.GRAY));
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
        for (Map.Entry<String, List<Component>> modGr: groupedModifiers.entrySet()) {
            for (Component mod: modGr.getValue()) {
                MutableComponent full = new TextComponent(useNums ? i + " " : "► ").withStyle(COLORS[i % COLORS.length]);
                full.append(mod);
                componentList.add(new GroupTextComponent((TextComponent) full, (TextComponent)new TextComponent(modGr.getKey()).withStyle(COLORS[i % COLORS.length])));
            }
            i++;
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

        ArrayList<VaultGearTierConfig.ModifierTier<?>> res = new ArrayList<>();
        VaultGearTierConfig.ModifierTier<?> highest = modifierTierGroup.getHighestForLevel(lvl);
        if (highest == null) {
            return res; // empty
        }
        if (modifierTierGroup.getTags().contains("noLegendary")){
            return res; // empty
        }
        int index = Math.min(highest.getModifierTier() + modifierCategory.getTierIncrease(), modifierTierGroup.size() - 1);
        VaultGearTierConfig.ModifierTier<?> legendTier = modifierTierGroup.get(index);
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
