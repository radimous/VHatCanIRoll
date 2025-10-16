package com.radimous.vhatcaniroll.mixin.accessors.cards;

import iskallia.vault.core.card.CardCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(value = CardCondition.class, remap = false)
public interface CardConditionAccessor {
    @Accessor
    Map<Integer, List<CardCondition.Filter>> getFilters();
}
