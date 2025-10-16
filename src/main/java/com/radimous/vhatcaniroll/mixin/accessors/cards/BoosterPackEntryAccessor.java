package com.radimous.vhatcaniroll.mixin.accessors.cards;


import iskallia.vault.config.card.BoosterPackConfig;
import iskallia.vault.core.card.CardEntry;
import iskallia.vault.core.util.WeightedList;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = BoosterPackConfig.BoosterPackEntry.class, remap = false)
public interface BoosterPackEntryAccessor {
    @Accessor
    Component getName();
    @Accessor
    BoosterPackConfig.BoosterPackModel getModel();
    @Accessor
    WeightedList<Integer> getRoll();
    @Accessor
    WeightedList<Integer> getTier();
    @Accessor
    WeightedList<CardEntry.Color> getColor();
}
