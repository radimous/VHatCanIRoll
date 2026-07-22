package com.radimous.vhatcaniroll.mixin.accessors.cards;

import iskallia.vault.config.card.DeckModifiersConfig;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import iskallia.vault.core.util.WeightedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = DeckModifiersConfig.class, remap = false)
public interface DeckModifiersConfigAccessor {
    @Accessor
    Map<String, DeckModifier<?>> getValues();

    @Accessor
    Map<String, WeightedList<String>> getPools();
}
