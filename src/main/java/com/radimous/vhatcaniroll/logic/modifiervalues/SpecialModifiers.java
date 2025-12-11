package com.radimous.vhatcaniroll.logic.modifiervalues;

import com.mojang.datafixers.util.Pair;
import com.radimous.vhatcaniroll.logic.ComponentUtil;
import com.radimous.vhatcaniroll.mixin.accessors.AbilityFloatValueAttributeReaderInvoker;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.ability.AbilityAreaOfEffectPercentAttribute;
import iskallia.vault.gear.attribute.ability.AbilityLevelAttribute;
import iskallia.vault.gear.attribute.ability.special.EntropyPoisonModification;
import iskallia.vault.gear.attribute.ability.special.FrostNovaVulnerabilityModification;
import iskallia.vault.gear.attribute.ability.special.base.SpecialAbilityConfig;
import iskallia.vault.gear.attribute.ability.special.base.SpecialAbilityGearAttribute;
import iskallia.vault.gear.attribute.ability.special.base.SpecialAbilityModification;
import iskallia.vault.gear.attribute.ability.special.base.template.FloatRangeModification;
import iskallia.vault.gear.attribute.ability.special.base.template.IntRangeModification;
import iskallia.vault.gear.attribute.ability.special.base.template.config.FloatRangeConfig;
import iskallia.vault.gear.attribute.ability.special.base.template.config.IntRangeConfig;
import iskallia.vault.gear.attribute.ability.special.base.template.value.FloatValue;
import iskallia.vault.gear.attribute.ability.special.base.template.value.IntValue;
import iskallia.vault.gear.attribute.config.*;
import iskallia.vault.gear.attribute.custom.RandomGodVaultModifierAttribute;
import iskallia.vault.gear.attribute.custom.ability.AbilityTriggerOnDamageAttribute;
import iskallia.vault.gear.attribute.custom.ability.ArcaneNovaOnHitAttribute;
import iskallia.vault.gear.attribute.custom.effect.EffectTrialAttribute;
import iskallia.vault.gear.attribute.custom.loot.AbilityCastOnLootAttribute;
import iskallia.vault.gear.attribute.custom.loot.ManaPerLootAttribute;
import iskallia.vault.gear.attribute.talent.RandomVaultModifierAttribute;
import iskallia.vault.gear.attribute.talent.TalentLevelAttribute;
import iskallia.vault.gear.reader.FloatValueModifierReader;
import iskallia.vault.gear.reader.IntegerValueModifierReader;
import iskallia.vault.gear.reader.VaultGearModifierReader;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.skill.ability.AbilityType;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.util.StringUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This handles very ugly parts
 */
public class SpecialModifiers {

    private static final Pattern CLOUD_PATTERN = Pattern.compile("^(?<effect>.*?) ?(?<lvl>I|II|III|IV|V|VI|VII|VIII|IX|X)? (?<suffix>Cloud.*)$");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.##");

    @SuppressWarnings("unchecked")
    static <T, C> MutableComponent getCustomComponent(VaultGearAttribute<T> atr, C minConfig,
                                            C maxConfig, String atrName, ConfigurableAttributeGenerator<T, C> atrGenerator) {
        // Custom based on attribute name
        if (atrName.equals("the_vault:relentless_strike")) {
            return getRelentlessStrikeComponent(atr, minConfig, maxConfig, atrGenerator);
        }
        if (atrName.equals("the_vault:third_attack")) {
            return getThirdAttackComponent(atr, minConfig, maxConfig, atrGenerator);
        }
        if (atrName.equals("the_vault:lucky_thorns")) {
            return getLuckyThornsComponent();
        }

        if (atrName.equals("the_vault:on_kill_heal") || atrName.equals("the_vault:block_heal") ) {
            if (minConfig instanceof PairAttributeGenerator.Config<?,?> pairAttributeGenerator) {
                var minPair =  atrGenerator.getMinimumValue(List.of(minConfig, maxConfig)).orElse(null) instanceof Pair pair ? (Pair<Float, Float>) pair : null ;
                var maxPair = atrGenerator.getMaximumValue(List.of(minConfig, maxConfig)).orElse(null) instanceof Pair pair ? (Pair<Float, Float>) pair : null ;
                var minChance = minPair.getFirst();
                var maxChance = maxPair.getFirst();
                var minHP = minPair.getSecond();
                var maxHP = maxPair.getSecond();

                MutableComponent component = new TextComponent("").withStyle(Style.EMPTY.withColor(14901010));
                float minHearts = minHP / 2.0F;
                float maxHearts = maxHP / 2.0F;
                String heartWord = maxHearts == 1.0F ? "heart" : "hearts";

                String chances;
                if (minChance.equals(maxChance)) {
                    chances = DECIMAL_FORMAT.format(minChance * 100.0F) + "%";
                } else {
                    chances = DECIMAL_FORMAT.format(minChance * 100.0F) + "%-" + DECIMAL_FORMAT.format(maxChance * 100.0F) + "%";
                }

                String hearts;
                if (minHearts == maxHearts) {
                    hearts = DECIMAL_FORMAT.format(minHearts) + " " + heartWord;
                } else {
                    hearts = DECIMAL_FORMAT.format(minHearts) + "-" + DECIMAL_FORMAT.format(maxHearts) + " "+ heartWord;
                }

                if (atrName.equals("the_vault:block_heal")) {
                    component.append(new TextComponent(chances).withStyle(ChatFormatting.YELLOW));
                    component.append(new TextComponent(" chance to gain "));
                    component.append(new TextComponent(hearts).withStyle(ChatFormatting.YELLOW));
                    component.append(new TextComponent(" when successfully landing a block"));
                } else if (atrName.equals("the_vault:on_kill_heal")) {
                    component.append(new TextComponent(chances).withStyle(ChatFormatting.YELLOW));
                    component.append(new TextComponent(" chance to gain "));
                    component.append(new TextComponent(hearts).withStyle(ChatFormatting.YELLOW));
                    component.append(new TextComponent(" when killing a mob, you can no longer gain health from any other sources"));
                }

                return component;
            }
        }

        // custom based on attribute class
        var customClassComponent = getCustomClassComponent(atr, minConfig, maxConfig, atrGenerator);
        if (customClassComponent != null) {
            return customClassComponent;
        }
        // wold's objectives without relying on wold's classes
        if (minConfig instanceof Supplier<?> supplier){
            try {
                var comp = atr.getReader().getValueDisplay((T) supplier.get());
                if (comp != null){
                    return comp.withStyle(ChatFormatting.WHITE);
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T, C> MutableComponent getCustomClassComponent(VaultGearAttribute<T> atr, C minConfig, C maxConfig,  ConfigurableAttributeGenerator<T, C> atrGenerator) {
        if (minConfig == null || maxConfig == null) {
            return null;
        }
        if (minConfig.getClass() != maxConfig.getClass()) {
            return null;
        }

        if (minConfig instanceof AbilityAreaOfEffectPercentAttribute.Config minConfigA && maxConfig instanceof AbilityAreaOfEffectPercentAttribute.Config maxConfigA) {
            return getAbilityAoePercentageComponent(atr, minConfigA, maxConfigA);
        }
        if (minConfig instanceof ManaPerLootAttribute.Config minManaPerLootConfig && maxConfig instanceof ManaPerLootAttribute.Config maxManaPerLootConfig) {
            return getManaPerLootComponent(minManaPerLootConfig, maxManaPerLootConfig);
        }
        if (minConfig instanceof AbilityTriggerOnDamageAttribute.Config minAbilityTriggerOnDamageConfig && maxConfig instanceof AbilityTriggerOnDamageAttribute.Config maxAbilityTriggerOnDamageConfig) {
            return getAbilityOnDamageComponent(minAbilityTriggerOnDamageConfig, maxAbilityTriggerOnDamageConfig);
        }
        if (minConfig instanceof AbilityCastOnLootAttribute.Config minAbilityCastOnLootConfig && maxConfig instanceof AbilityCastOnLootAttribute.Config maxAbilityCastOnLootConfig) {
            return getAbilityCastOnLootComponent(minAbilityCastOnLootConfig, maxAbilityCastOnLootConfig);
        }
        if (minConfig instanceof RandomGodVaultModifierAttribute.Config minGod && maxConfig instanceof RandomGodVaultModifierAttribute.Config maxGod) {
            return getRandomGodVaultModifierAttributeComponent(minGod, maxGod);
        }
        if (minConfig instanceof EffectTrialAttribute.Config minTrail && maxConfig instanceof EffectTrialAttribute.Config maxTrail) {
            return getEffectTrailComponent(minTrail, maxTrail);
        }
        if (minConfig instanceof ArcaneNovaOnHitAttribute.Config minArcane && maxConfig instanceof ArcaneNovaOnHitAttribute.Config maxArcane) {
            return getArcaneNovaComponent(atr, minArcane, maxArcane);
        }

        if (minConfig instanceof RandomVaultModifierAttribute.Config minTemporal && maxConfig instanceof RandomVaultModifierAttribute.Config maxTemporal) {

            var modifierId = minTemporal.getModifier();
            var modifier = VaultModifierRegistry.getOpt(modifierId).orElse(null);
            if (modifier == null) {
                return new TextComponent("modifier " + modifierId + " not found in modifier registry").withStyle(ChatFormatting.RED);
            }
            return new TextComponent("+" + (minTemporal.getTime().getMin() / 20) + "s-" + (maxTemporal.getTime().getMax() / 20) + "s of ").append(modifier.getNameComponentFormatted(minTemporal.getCount())).withStyle(atr.getReader().getColoredTextStyle());
        }
        if (minConfig instanceof IntegerAttributeGenerator.Range) {
            return getIntegerValueComponent(atr, minConfig, maxConfig, atrGenerator);
        }
        if (minConfig instanceof FloatAttributeGenerator.Range) {
            var range = atrGenerator.getConfigRangeDisplay(atr.getReader(), minConfig, maxConfig);
            if (range == null) return null;
            var reader = atr.getReader();
            if (reader instanceof FloatValueModifierReader floatReader) {
                var inst = new VaultGearAttributeInstance<>(atr, (T)(Object)42069F);
                var fl = floatReader.getValueDisplay(42069F);
                var kk = reader.getDisplay(inst, VaultGearModifier.AffixType.PREFIX);
                if (fl != null){
                    return (MutableComponent) ComponentUtil.replace(kk, fl.getString(), (TextComponent) range);
                }
            }
            return null;
        }
        return null;
    }

    private static <T>  MutableComponent getArcaneNovaComponent(VaultGearAttribute<T> atr, ArcaneNovaOnHitAttribute.Config minArcane,
                                                                     ArcaneNovaOnHitAttribute.Config maxArcane) {
        var minHits = minArcane.getHitsRequired().getMin();
        var maxHits = maxArcane.getHitsRequired().getMax();
        String minVal = new DecimalFormat("0.##").format(minArcane.getPercentAbilityPower().getMin() * 100.0F) + "%";
        String maxVal = new DecimalFormat("0.##").format(maxArcane.getPercentAbilityPower().getMax() * 100.0F) + "%";
        String minRadius = new DecimalFormat("0.#").format(minArcane.getRadius().getMin());
        String maxRadius = new DecimalFormat("0.#").format(maxArcane.getRadius().getMax());
        return (new TextComponent(""))
            .append((new TextComponent("Every ")).withStyle(atr.getReader().getColoredTextStyle()))
            .append((new TextComponent(minHits + "-" + maxHits)).withStyle(Style.EMPTY.withColor(ModConfigs.COLORS.getColor("uniqueHighlight"))))
            .append((new TextComponent(" hits unleash an Arcane nova dealing ")).withStyle(atr.getReader().getColoredTextStyle()))
            .append(new TextComponent(minVal + "-" + maxVal).withStyle(Style.EMPTY.withColor(ModConfigs.COLORS.getColor("uniqueHighlight"))))
            .append((new TextComponent(" of your Ability Power in a ")).withStyle(atr.getReader().getColoredTextStyle()))
            .append((new TextComponent(minRadius + "-" + maxRadius).withStyle(Style.EMPTY.withColor(ModConfigs.COLORS.getColor("uniqueHighlight"))))
                .append((new TextComponent(" block radius")).withStyle(atr.getReader().getColoredTextStyle())));
    }

    static MutableComponent getRandomGodVaultModifierAttributeComponent(RandomGodVaultModifierAttribute.Config minConfig, RandomGodVaultModifierAttribute.Config maxConfig) {
        var reader = RandomGodVaultModifierAttribute.reader();
            var type = VaultGearModifier.AffixType.PREFIX;
        var minTime = minConfig.getTime();
        var maxTime = minConfig.getTime();
        TextComponent valueDisplay;
        if (minTime == maxTime){
            valueDisplay = new TextComponent(minTime.getMin() / 20 + " seconds");
        } else {
            valueDisplay = new TextComponent((minTime.getMin() / 20) + "-" + (maxTime.getMax() / 20) + " seconds");
        }
        VaultModifier<?> modifier = VaultModifierRegistry.getOpt(minConfig.getModifier()).orElse(null);
        if (modifier == null) {
            return new TextComponent("Random Vault God Modifier - NULL ERROR when reading modifier type").withStyle(Style.EMPTY.withColor(14901010));
        }
        return (new TextComponent("")).append(type.getAffixPrefixComponent(minConfig.getCount() >= 0).withStyle(reader.getColoredTextStyle())).append(valueDisplay.withStyle(reader.getColoredTextStyle())).append(" of ").append(minConfig.getCount() > 1 ? minConfig.getCount() + "x " : "").append(modifier.getNameComponentFormatted(minConfig.getCount())).withStyle(reader.getColoredTextStyle());

    }

    static MutableComponent getEffectTrailComponent(EffectTrialAttribute.Config minConfig, EffectTrialAttribute.Config maxConfig){

        var type = VaultGearModifier.AffixType.PREFIX;

        var effectId = minConfig.getEffectId();
        var minDurationTicks = minConfig.getDurationTicks().getMin();
        var maxDurationTicks = maxConfig.getDurationTicks().getMax();

        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectId);
        if (effect == null) {
            return null;
        } else {
            MutableComponent var10000 = type.getAffixPrefixComponent(true).append(new TextComponent("Leaves a trail of ")).append((new TranslatableComponent(effect.getDescriptionId())).withStyle(Style.EMPTY.withColor(16755200))).append(new TextComponent(" for "));
            DecimalFormat df = new DecimalFormat("0.##");
            return var10000.append((new TextComponent(df.format(minDurationTicks / (double)20.0F) + "s").append(new TextComponent("-")).append(new TextComponent(df.format(maxDurationTicks / (double)20.0F) + "s"))).withStyle(Style.EMPTY.withColor(16755200))).setStyle(Style.EMPTY.withColor(14901010));
        }
    }

    static MutableComponent getAbilityOnDamageComponent(AbilityTriggerOnDamageAttribute.Config minConfig, AbilityTriggerOnDamageAttribute.Config maxConfig) {
        MutableComponent range = AbilityTriggerOnDamageAttribute.generator().getConfigRangeDisplay(AbilityTriggerOnDamageAttribute.reader(), minConfig, maxConfig);
        if (range == null) return null;
        var rangeSiblings = range.getSiblings();
        MutableComponent abilityName = new TextComponent(ModConfigs.ABILITIES.getAbilityById(minConfig.getAbilityId()).map(Skill::getName).orElse(""));
        var color = 14901010;
        return new TextComponent("").withStyle(Style.EMPTY.withColor(color))
            .append("Every hit you take has a ")
            .append(new TextComponent(((TextComponent)range).getText() +rangeSiblings.get(0).getString()+rangeSiblings.get(1).getString()).withStyle(Style.EMPTY.withColor(16755200)))
            .append(" chance to cast a level ")
            .append(new TextComponent(rangeSiblings.get(3).getContents()).withStyle(Style.EMPTY.withColor(16755200)).append(rangeSiblings.get(4).getContents()).append(rangeSiblings.get(5).getContents()))
            .append(" ")
            .append(abilityName.withStyle(Style.EMPTY.withColor(color)));

    }

    static MutableComponent getAbilityCastOnLootComponent(AbilityCastOnLootAttribute.Config minConfig, AbilityCastOnLootAttribute.Config maxConfig) {
        MutableComponent range = AbilityCastOnLootAttribute.generator().getConfigRangeDisplay(AbilityCastOnLootAttribute.reader(), minConfig, maxConfig);
        if (range == null) return null;
        var rangeSiblings = range.getSiblings();
        MutableComponent abilityName = new TextComponent(ModConfigs.ABILITIES.getAbilityById(minConfig.getAbilityId()).map(Skill::getName).orElse(""));
        var color = 14901010;
        return new TextComponent("").withStyle(Style.EMPTY.withColor(color))
            .append("Looting has a ")
            .append(new TextComponent(((TextComponent)range).getText() +rangeSiblings.get(0).getString()+rangeSiblings.get(1).getString()).withStyle(Style.EMPTY.withColor(16755200)))
            .append(" chance to cast a level ")
            .append(new TextComponent(rangeSiblings.get(3).getContents()).withStyle(Style.EMPTY.withColor(16755200)).append(rangeSiblings.get(4).getContents()).append(rangeSiblings.get(5).getContents()))
            .append(new TextComponent(" "))
            .append(abilityName.withStyle(Style.EMPTY.withColor(color)));

    }

    /**
     * {@link iskallia.vault.gear.reader.special.ThirdAttackModifierReader}
     */
    private static <T, C> MutableComponent getThirdAttackComponent(VaultGearAttribute<T> atr, C minConfig, C maxConfig, ConfigurableAttributeGenerator<T,C> atrGenerator) {
        return new TextComponent("")
            .append(new TextComponent("Every third attack deals "))
            .append((atrGenerator.getConfigRangeDisplay(atr.getReader(), minConfig, maxConfig)).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(16755200))))
            .append(new TextComponent(" increased physical damage"))
            .setStyle(ModGearAttributes.THIRD_ATTACK.getReader().getColoredTextStyle());

    }

    private static <T, C> MutableComponent getIntegerValueComponent(VaultGearAttribute<T> atr, C minConfig, C maxConfig, ConfigurableAttributeGenerator<T,C> atrGenerator) {
        var range = atrGenerator.getConfigRangeDisplay(atr.getReader(), minConfig, maxConfig);
        if (range == null) return null;
        var reader = atr.getReader();
        if (reader instanceof IntegerValueModifierReader intReader) {
            var inst = new VaultGearAttributeInstance<>(atr, (T)(Object)42069);
            var in = intReader.getValueDisplay(42069);
            var kk = reader.getDisplay(inst, VaultGearModifier.AffixType.PREFIX);
            if (in != null) {
                return (MutableComponent) ComponentUtil.replace(kk, in.getString(), (TextComponent) range);
            }
        }
        return null;
    }

    /**
     * {@link iskallia.vault.gear.reader.special.LuckyThornsModifierReader}
     */
    private static <T> MutableComponent getLuckyThornsComponent() {
        return new TextComponent("")
            .append((new TextComponent("Thorns ")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(16755200))))
            .append((new TextComponent("can now trigger ")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(14901010))))
            .append((new TextComponent("Lucky Hits")).withStyle(Style.EMPTY.withColor(ModConfigs.COLORS.getColor("luckyHit"))))
            .setStyle(ModGearAttributes.LUCKY_THORNS.getReader().getColoredTextStyle());
    }

    /**
     * {@link iskallia.vault.gear.reader.special.RelentlessStrikeModifierReader}
     */
    private static <T, C> MutableComponent getRelentlessStrikeComponent(VaultGearAttribute<T> atr, C minConfig, C maxConfig, ConfigurableAttributeGenerator<T,C> atrGenerator) {
        return new TextComponent("")
            .append(new TextComponent("Attacking a mob increases your attack damage by "))
            .append((atrGenerator.getConfigRangeDisplay(atr.getReader(), minConfig, maxConfig)).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(16755200))))
            .append(new TextComponent(", this effect can stack up to "))
            .append((new TextComponent("9")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(16755200))))
            .append(new TextComponent(" times and lasts for "))
            .append((new TextComponent("8 seconds")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(16755200))))
            .setStyle(ModGearAttributes.RELENTLESS_STRIKE.getReader().getColoredTextStyle());
    }


    static @NotNull MutableComponent getManaPerLootComponent(ManaPerLootAttribute.Config minManaPerLootConfig,
                                                             ManaPerLootAttribute.Config maxManaPerLootConfig) {
        int minGenerated = minManaPerLootConfig.getManaGenerated().getMin();
        int maxGenerated = maxManaPerLootConfig.getManaGenerated().getMax();
        float minGenChance = minManaPerLootConfig.getManaGenerationChance().getMin();
        float maxGenChance = maxManaPerLootConfig.getManaGenerationChance().getMax();
        var minToMax = new TextComponent(DECIMAL_FORMAT.format(minGenChance*100) + "%-" + DECIMAL_FORMAT.format(maxGenChance*100) + "%").withStyle(Style.EMPTY.withColor(20479));
        var generated = new TextComponent(minGenerated + "-" + maxGenerated).withStyle(Style.EMPTY.withColor(20479));
        var loot = new TextComponent(maxManaPerLootConfig.getDisplayName()).withStyle(Style.EMPTY.withColor(20479));
        return new TextComponent("").withStyle(Style.EMPTY.withColor(65535)).append(minToMax).append(new TextComponent(" chance to generate ")).append(generated).append(" Mana per ").append(loot).append(" looted");
    }

    static <T> @NotNull MutableComponent getAbilityAoePercentageComponent(VaultGearAttribute<T> atr,
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

    static MutableComponent getCloudRangeComponent(MutableComponent minConfigDisplay, MutableComponent maxConfigDisplay, VaultGearAttribute<?> atr) {
        // <Effect> [<LVL>] Cloud [when Hit]
        // Poison Cloud
        // Poison III Cloud
        String minString = minConfigDisplay.getString();
        String maxString = maxConfigDisplay.getString();

        String minLvl = getCloudLvl(minString);
        String maxLvl = getCloudLvl(maxString);

        if (minLvl.equals(maxLvl)) {
            return minConfigDisplay.withStyle(atr.getReader().getColoredTextStyle());
        }

        String cloudRange = makeCloudLvlRange(minString, minLvl, maxLvl);
        return new TextComponent(cloudRange).withStyle(atr.getReader().getColoredTextStyle());
    }

    private static String getCloudLvl(String displayString){
        Matcher matcher = CLOUD_PATTERN.matcher(displayString);
        if (matcher.find()) {
            if (matcher.group("lvl") != null) {
                return matcher.group("lvl");
            }
            return "I";
        }
        return "I";
    }

    private static String makeCloudLvlRange(String displayString, String minLvl, String maxLvl){
        Matcher matcher = CLOUD_PATTERN.matcher(displayString);
        if (matcher.find()) {
            return matcher.group("effect") + " " + minLvl + "-" + maxLvl + " " + matcher.group("suffix");
        }
        return displayString;
    }

    static MutableComponent abilityLvlComponent(MutableComponent prev, VaultGearAttribute<?> atr,
                                                AbilityLevelAttribute.Config minConfig) {

        MutableComponent abComp = new TextComponent("+").withStyle(atr.getReader().getColoredTextStyle());
        String[] parts = prev.getString().split("-");

        MutableComponent res = new TextComponent("").withStyle(prev.getStyle());
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
        if (minConfig.getAbilityKey().equals("all_abilities")){
            return abComp.append(new TextComponent("All Abilities").withStyle(Style.EMPTY.withColor(14076214)));
        }
        if (AbilityType.matches(minConfig.getAbilityKey())) {
            return abComp.append("all ")
                .append(new TextComponent(StringUtils.convertToTitleCase(minConfig.getAbilityKey())).withStyle(Style.EMPTY.withColor(14076214)))
                .append(" abilities");
        }
        Optional<Skill> optSkill = ModConfigs.ABILITIES.getAbilityById(minConfig.getAbilityKey());
        if (optSkill.isEmpty()) {
            return abComp.append(new TextComponent("ABILITY NOT FOUND - id="+minConfig.getAbilityKey()).withStyle(ChatFormatting.RED));
        }
        String abName = optSkill.get().getName();
        abComp.append(new TextComponent(abName).withStyle(Style.EMPTY.withColor(14076214)));
        return abComp;
    }

    static MutableComponent talentLvlComponent(MutableComponent prev, VaultGearAttribute<?> atr,
                                                TalentLevelAttribute.Config minConfig) {

        MutableComponent talComp = new TextComponent("+").withStyle(atr.getReader().getColoredTextStyle());

        String[] parts = prev.getString().split("-");

        MutableComponent res = new TextComponent("").withStyle(prev.getStyle());
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
        talComp.append(res);
        talComp.append(" to level of ");
        if (minConfig.getTalent().equals("all_talents")){
            return talComp.append(new TextComponent("All Talents").withStyle(Style.EMPTY.withColor(14076214)));
        }
        Optional<Skill> optSkill = ModConfigs.TALENTS.getTalentById(minConfig.getTalent());
        if (optSkill.isEmpty()) {
            return talComp.append(new TextComponent("TALENT NOT FOUND - id="+minConfig.getTalent()).withStyle(ChatFormatting.RED));
        }
        String talName = optSkill.get().getName();
        talComp.append(new TextComponent(talName).withStyle(Style.EMPTY.withColor(14076214)));
        return talComp;
    }


    @SuppressWarnings("unchecked") // this thing is insane
    static @NotNull MutableComponent getSpecialAbilityAttributeComponent(
        ArrayList<VaultGearTierConfig.ModifierTier<?>> modifierTiers,
        SpecialAbilityGearAttribute.SpecialAbilityTierConfig<?, ?, ?> minConfigSpecial) {
        SpecialAbilityModification<? extends SpecialAbilityConfig<?>, ?> modification = minConfigSpecial.getModification();
        if (modification instanceof IntRangeModification intRangeModification){
            var minValue = intRangeModification.getMinimumValue(getIntTiers(modifierTiers));
            var maxValue = intRangeModification.getMaximumValue(getIntTiers(modifierTiers));
            String minValueDisplay = minValue.map(x -> String.valueOf(x.getValue().getValue())).orElse("NULL");
            String maxValueDisplay = maxValue.map(x -> String.valueOf(x.getValue().getValue())).orElse("NULL");
            MutableComponent minToMaxComponent = new TextComponent(minValueDisplay + "-" + maxValueDisplay).withStyle(ChatFormatting.UNDERLINE).withStyle(Style.EMPTY.withColor(
                TextColor.fromRgb(6082075)));
            if (intRangeModification instanceof FrostNovaVulnerabilityModification) {
                return (new TextComponent("Frost Nova also applies Level ").append(minToMaxComponent).append(" Vulnerability")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(14076214)));
            }
            if (intRangeModification instanceof EntropyPoisonModification) {
                return new TextComponent("Entropic Bind also applies Poison ").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(14076214))).append(minToMaxComponent);
            }
            if (intRangeModification.getKey().toString().equals("the_vault:glacial_blast_hypothermia")){
                return (new TextComponent("Glacial Blast is ").append(minToMaxComponent).append("X more likely to shatter")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(14076214)));
            }

        }
        if (modification instanceof FloatRangeModification floatRangeModification) {
            var minValue = floatRangeModification.getMinimumValue(getFloatTiers(modifierTiers));
            var maxValue = floatRangeModification.getMaximumValue(getFloatTiers(modifierTiers));
            float minValueDisplay = minValue.map(x -> x.getValue().getValue()).orElse(0f);
            float maxValueDisplay = maxValue.map(x -> x.getValue().getValue()).orElse(0f);

            MutableComponent minToMaxComponent = new TextComponent(DECIMAL_FORMAT.format(minValueDisplay*100) + "%-" + DECIMAL_FORMAT.format(maxValueDisplay*100)+"%").withStyle(ChatFormatting.UNDERLINE).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(6082075)));
            if (floatRangeModification.getKey().toString().equals("the_vault:fireball_special_modification")){
                return (new TextComponent("Fireball has ").append(minToMaxComponent).append(" chance to fire twice")).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(14076214)));
            }

        }


        String abilityKey = minConfigSpecial.getAbilityKey();
        return ModConfigs.ABILITIES.getAbilityById(abilityKey).filter(skill -> skill.getName() != null).map(skill -> {
            String name = skill.getName();
            return new TextComponent("Special " + name + " modification");
        }).orElseGet(() -> (TextComponent) new TextComponent(abilityKey).withStyle(Style.EMPTY.withColor(14076214)));
    }

    @SuppressWarnings("unchecked")
    static List<SpecialAbilityGearAttribute.SpecialAbilityTierConfig<SpecialAbilityModification<IntRangeConfig, IntValue>, IntRangeConfig, IntValue>> getIntTiers(
        List<VaultGearTierConfig.ModifierTier<?>> modifierTiers) {
        return modifierTiers.stream().map(x -> (SpecialAbilityGearAttribute.SpecialAbilityTierConfig<SpecialAbilityModification<IntRangeConfig, IntValue>, IntRangeConfig, IntValue>) x.getModifierConfiguration()).toList();
    }

    @SuppressWarnings("unchecked")
    static List<SpecialAbilityGearAttribute.SpecialAbilityTierConfig<SpecialAbilityModification<FloatRangeConfig, FloatValue>, FloatRangeConfig, FloatValue>> getFloatTiers(
        List<VaultGearTierConfig.ModifierTier<?>> modifierTiers) {
        return modifierTiers.stream().map(x -> (SpecialAbilityGearAttribute.SpecialAbilityTierConfig<SpecialAbilityModification<FloatRangeConfig, FloatValue>, FloatRangeConfig, FloatValue>) x.getModifierConfiguration()).toList();
    }

}
