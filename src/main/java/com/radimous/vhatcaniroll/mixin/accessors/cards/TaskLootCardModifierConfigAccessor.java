package com.radimous.vhatcaniroll.mixin.accessors.cards;

import iskallia.vault.core.card.modifier.card.TaskLootCardModifier;
import iskallia.vault.core.world.loot.LootPool;
import iskallia.vault.core.world.roll.IntRoll;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = TaskLootCardModifier.Config.class, remap = false)
public interface TaskLootCardModifierConfigAccessor {
    @Accessor
    LootPool getLoot();

    @Accessor
    String getTask();

    @Accessor
    Map<Integer, IntRoll> getCount();

    @Accessor
    Component getTooltip();

    @Accessor
    int getHighlightColor();
}
