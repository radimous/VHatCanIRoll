package com.radimous.vhatcaniroll.mixin.accessors.cards;

import iskallia.vault.config.card.CardConditionsConfig;
import iskallia.vault.core.card.CardCondition;
import iskallia.vault.core.util.WeightedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = CardConditionsConfig.class, remap = false)
public interface CardConditionsConfigAccessor {
    @Accessor
    Map<String, CardCondition> getValues();

    @Accessor
    Map<String, WeightedList<String>> getPools();
}
