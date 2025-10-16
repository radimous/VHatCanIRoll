package com.radimous.vhatcaniroll.mixin.accessors.cards;

import iskallia.vault.core.card.CardCondition;
import iskallia.vault.core.util.WeightedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(value = CardCondition.Config.class, remap = false)
public interface CardConditionConfigAccessor {

    @Accessor
    Map<Integer, WeightedList<List<CardCondition.Filter.Config>>> getTiers();
}
