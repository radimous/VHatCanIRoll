package com.radimous.vhatcaniroll.mixin.accessors.cards;

import iskallia.vault.core.card.CardEntry;
import iskallia.vault.core.card.CardNeighborType;
import iskallia.vault.core.card.CardScaler;
import iskallia.vault.core.util.WeightedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(value = CardScaler.Filter.Config.class, remap = false)
public interface CardScalerFilterConfigAccessor {
    @Accessor
    WeightedList<Set<CardNeighborType>> getNeighborFilter();

    @Accessor
    WeightedList<Set<Integer>> getTierFilter();

    @Accessor
    WeightedList<Set<CardEntry.Color>> getColorFilter();

    @Accessor
    WeightedList<Set<String>> getGroupFilter();
}
