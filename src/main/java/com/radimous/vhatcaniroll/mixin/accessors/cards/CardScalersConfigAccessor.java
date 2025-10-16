package com.radimous.vhatcaniroll.mixin.accessors.cards;

import iskallia.vault.config.card.CardScalersConfig;
import iskallia.vault.core.card.CardScaler;
import iskallia.vault.core.util.WeightedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = CardScalersConfig.class, remap = false)
public interface CardScalersConfigAccessor {
    @Accessor
    Map<String, WeightedList<String>> getPools();

    @Accessor
    Map<String, CardScaler> getValues();
}
