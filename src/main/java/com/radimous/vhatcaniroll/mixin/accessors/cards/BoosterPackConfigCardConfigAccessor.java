package com.radimous.vhatcaniroll.mixin.accessors.cards;

import iskallia.vault.config.card.BoosterPackConfig;
import iskallia.vault.core.card.CardEntry;
import iskallia.vault.core.util.WeightedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Set;

@Mixin(value = BoosterPackConfig.CardConfig.class, remap = false)
public interface BoosterPackConfigCardConfigAccessor {

    @Accessor
    WeightedList<List<CardEntry.Color>> getColors();

    @Accessor
    Set<String> getGroups();

    @Accessor
    String getScaler();

    @Accessor
    String getCondition();

    @Accessor
    double getProbability();
}
