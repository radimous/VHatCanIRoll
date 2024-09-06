package com.radimous.vhatcaniroll;

import com.radimous.vhatcaniroll.mixin.VaultGearTierConfigAccessor;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.attribute.config.BooleanFlagGenerator;
import iskallia.vault.gear.attribute.config.ConfigurableAttributeGenerator;
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

    public static List<Component> getModifierList(int lvl, VaultGearTierConfig cfg) {
        Map<VaultGearTierConfig.ModifierAffixTagGroup, VaultGearTierConfig.AttributeGroup> modifierGroup =
            ((VaultGearTierConfigAccessor) cfg).getModifierGroup();
        ArrayList<Component> modList = new ArrayList<>();
        for (VaultGearTierConfig.ModifierAffixTagGroup affixTagGroup : modifierGroup.keySet()) {
            processAffixTagGroup(lvl, affixTagGroup, modifierGroup, modList);
        }
        return modList;
    }

    private static void processAffixTagGroup(int lvl, VaultGearTierConfig.ModifierAffixTagGroup affixTagGroup,
                                             Map<VaultGearTierConfig.ModifierAffixTagGroup, VaultGearTierConfig.AttributeGroup> modifierGroup,
                                             ArrayList<Component> modList) {
        if (affixTagGroup.equals(VaultGearTierConfig.ModifierAffixTagGroup.ABILITY_ENHANCEMENT)) {
            return;
        }
        if (modifierGroup.get(affixTagGroup).isEmpty()) {
            return;
        }
        modList.add(new TextComponent(affixTagGroup.toString().replace("_", " ")).withStyle(ChatFormatting.BOLD));

        Map<String, Integer> groupCounts = countGroups(lvl, affixTagGroup, modifierGroup);

        List<String> grList = new ArrayList<>();
        for (VaultGearTierConfig.ModifierTierGroup modifierTierGroup : modifierGroup.get(affixTagGroup)) {
            ArrayList<VaultGearTierConfig.ModifierTier<?>> mTierList = getModifierTiers(lvl, modifierTierGroup);
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
                                                    Map<VaultGearTierConfig.ModifierAffixTagGroup, VaultGearTierConfig.AttributeGroup> modifierGroup) {
        Map<String, Integer> groupCounts = new HashMap<>();
        for (VaultGearTierConfig.ModifierTierGroup modifierTierGroup : modifierGroup.get(affixTagGroup)) {
            ArrayList<VaultGearTierConfig.ModifierTier<?>> mTierList = getModifierTiers(lvl, modifierTierGroup);
            if (mTierList.isEmpty()) {
                continue;
            }
            groupCounts.put(modifierTierGroup.getModifierGroup(),
                groupCounts.getOrDefault(modifierTierGroup.getModifierGroup(), 0) + 1);
        }
        return groupCounts;
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
        MutableComponent res = null;
        if (val.size() > 1) {
            res = atrGenerator.getConfigRangeDisplay(atr.getReader(), minConfig, maxConfig);
        }
        ResourceLocation atrRegName = atr.getRegistryName();
        if (atrRegName == null) {
            return new TextComponent("ERR - NULL REGISTRY NAME");
        }
        String atrName = atrRegName.toString();
        var minConfigDisplay = atrGenerator.getConfigDisplay(atr.getReader(), minConfig);

        if (res == null && minConfigDisplay != null) {
            res = minConfigDisplay.withStyle(atr.getReader().getColoredTextStyle());
            if (atrName.equals("the_vault:added_ability_level")) {
                return res.append(" added ability levels").withStyle(atr.getReader().getColoredTextStyle());
            }
            if (atrName.equals("the_vault:wendarr_affinity")) {
                return TextComponentUtils.replace(TextComponentUtils.createSourceStack(LogicalSide.CLIENT), res,
                        "Wendarr Affinity", new TextComponent("God Affinity"))
                    .withStyle(atr.getReader().getColoredTextStyle());
            }
            return res;
        }
        if (res != null && atrName.equals("the_vault:added_ability_level")) {
            return res.append(" added ability levels").withStyle(atr.getReader().getColoredTextStyle());
        }
        if (res != null && atrName.equals("the_vault:wendarr_affinity")) {
            return res.append(" God Affinity").withStyle(atr.getReader().getColoredTextStyle());
        }
        if (atrName.equals("the_vault:effect_avoidance") && minConfigDisplay != null) {
            var single = minConfigDisplay.withStyle(atr.getReader().getColoredTextStyle());
            res.append(single.getString().replace(minConfigDisplay.getString(), ""));
        }
        return atr.getReader().formatConfigDisplay(LogicalSide.CLIENT, res);
    }
}
