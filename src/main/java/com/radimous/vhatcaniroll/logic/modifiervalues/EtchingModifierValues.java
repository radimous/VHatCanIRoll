package com.radimous.vhatcaniroll.logic.modifiervalues;

import com.mojang.datafixers.util.Pair;
import com.radimous.vhatcaniroll.mixin.accessors.DoubleRangeAccessor;
import com.radimous.vhatcaniroll.mixin.accessors.FloatRangeAccessor;
import iskallia.vault.config.gear.EtchingTierConfig;
import iskallia.vault.config.gear.VaultEtchingConfig;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.config.*;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.util.TextUtil;
import iskallia.vault.util.Throuple;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Optional;

import static com.radimous.vhatcaniroll.VHatCanIRoll.ERROR_STYLE;

public class EtchingModifierValues {

    @SuppressWarnings("unchecked")
    public static <T, C> MutableComponent getEtchingComponent(VaultEtchingConfig.EtchingEntry eCfg, VaultGearAttribute<T> atr,
                                                              ArrayList<EtchingTierConfig.EtchingModifierTier<?>> modifierTiers) {
        if (eCfg == null) {
            return new TextComponent("ERR - NULL ETCHING CONFIG").withStyle(ERROR_STYLE);
        }

        if (modifierTiers.isEmpty()) {
            return new TextComponent("ERR - EMPTY MODIFIER TIERS").withStyle(ERROR_STYLE);
        }

        if (atr == null) {
            return new TextComponent("ERR - NULL ATTRIBUTE").withStyle(ERROR_STYLE);
        }

        ResourceLocation atrRegName = atr.getRegistryName();
        if (atrRegName == null) {
            return new TextComponent("ERR - NULL REGISTRY NAME").withStyle(ERROR_STYLE);
        }
        String atrName = atrRegName.toString();

        ConfigurableAttributeGenerator<T, C> atrGenerator = (ConfigurableAttributeGenerator<T, C>) atr.getGenerator();
        if (atrGenerator == null) {
            return new TextComponent("ERR - NULL ATTRIBUTE GENERATOR - " + atrName).withStyle(ERROR_STYLE);
        }

        C minConfig = (C) modifierTiers.get(0).getModifierConfiguration();
        C maxConfig = (C) modifierTiers.get(modifierTiers.size() - 1).getModifierConfiguration();


        // THROUPLE
        if (minConfig instanceof ThroupleAttributeGenerator.Config minThroupleConfig
            && maxConfig instanceof ThroupleAttributeGenerator.Config maxThroupleConfig) {
            try {
                return TextUtil.applyColorTags(RangedEtchingHelper.formatDescription(eCfg.getDescription(),
                    Optional.of(new Throuple<>(prepMin(minThroupleConfig.first), prepMin(minThroupleConfig.second), prepMin(minThroupleConfig.third))),
                    Optional.of(new Throuple<>(prepMax(maxThroupleConfig.first), prepMax(maxThroupleConfig.second), prepMax(maxThroupleConfig.third)))));
            } catch (Exception e) {
                return new TextComponent("ERR (throuple) - " + e.getMessage()).withStyle(ERROR_STYLE);
            }
        }

        // PAIR
        if (minConfig instanceof PairAttributeGenerator.Config minPairConfig
            && maxConfig instanceof PairAttributeGenerator.Config maxPairConfig) {
            try {
                return TextUtil.applyColorTags(RangedEtchingHelper.formatDescription(eCfg.getDescription(),
                    Optional.of(new Pair<>(prepMin(minPairConfig.first), prepMin(minPairConfig.second))),
                    Optional.of(new Pair<>(prepMax(maxPairConfig.first), prepMax(maxPairConfig.second)))));
            } catch (Exception e) {
                return new TextComponent("ERR (pair) - " + e.getMessage()).withStyle(ERROR_STYLE);
            }
        }

        try { // ANYTHING ELSE
            if (minConfig instanceof FloatAttributeGenerator.Range minfloatRange && maxConfig instanceof FloatAttributeGenerator.Range maxFloatRange) {
                return TextUtil.applyColorTags(RangedEtchingHelper.formatDescription(eCfg.getDescription(), Optional.of(((FloatRangeAccessor)minfloatRange).getMin()), Optional.of(((FloatRangeAccessor)maxFloatRange).getMax())));
            } else if (minConfig instanceof DoubleAttributeGenerator.Range minDoubleRange && maxConfig instanceof DoubleAttributeGenerator.Range maxDoubleRange) {
                return TextUtil.applyColorTags(RangedEtchingHelper.formatDescription(eCfg.getDescription(), Optional.of(((DoubleRangeAccessor)minDoubleRange).getMin()), Optional.of(((DoubleRangeAccessor)maxDoubleRange).getMax())));
            } else if (minConfig instanceof IntegerAttributeGenerator.Range minIntRange && maxConfig instanceof IntegerAttributeGenerator.Range maxIntRange) {
                return TextUtil.applyColorTags(RangedEtchingHelper.formatDescription(eCfg.getDescription(), Optional.of(minIntRange.min), Optional.of(maxIntRange.max)));
            } else if (minConfig instanceof BooleanFlagGenerator.BooleanFlag booleanFlag) {
                return TextUtil.applyColorTags(RangedEtchingHelper.formatDescription(eCfg.getDescription(), Optional.of(booleanFlag.get()), Optional.of(booleanFlag.get())));
            } else if (minConfig instanceof WeightedListAttributeGenerator.Config minWeightedList && maxConfig instanceof WeightedListAttributeGenerator.Config maxWeightedList) {
                return TextUtil.applyColorTags(RangedEtchingHelper.formatDescription(eCfg.getDescription(), Optional.of(prepMin(minWeightedList.strings)), Optional.of(prepMax(maxWeightedList.strings))));
            } else {
                var enclosing = minConfig.getClass().getEnclosingClass();
                var className = minConfig.getClass().getSimpleName();
                if (enclosing != null) {
                    className = enclosing.getSimpleName() + "$" + className;
                }
                return new TextComponent("ERR - " + className + " not supported yet").withStyle(ERROR_STYLE);
            }
        } catch (Exception e) {
            return new TextComponent("ERR - " + e.getMessage());
        }
    }

    private static Object prepMin(Object obj) {
        if (obj instanceof FloatAttributeGenerator.Range floatRange) {
            return ((FloatRangeAccessor)floatRange).getMin();
        }
        if (obj instanceof DoubleAttributeGenerator.Range doubleRange) {
            return ((DoubleRangeAccessor)doubleRange).getMin();
        }
        if (obj instanceof IntegerAttributeGenerator.Range intRange) {
            return intRange.min;
        }
        if (obj instanceof WeightedListAttributeGenerator.Config) {
            return obj;
        }
        return obj;
    }

    private static Object prepMax(Object obj) {
        if (obj instanceof FloatAttributeGenerator.Range floatRange) {
            return ((FloatRangeAccessor)floatRange).getMax();
        }
        if (obj instanceof DoubleAttributeGenerator.Range doubleRange) {
            return ((DoubleRangeAccessor)doubleRange).getMax();
        }
        if (obj instanceof IntegerAttributeGenerator.Range intRange) {
            return intRange.max;
        }
        if (obj instanceof WeightedListAttributeGenerator.Config) {
            return obj;
        }
        return obj;
    }

}
