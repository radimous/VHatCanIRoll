package com.radimous.vhatcaniroll.mixin;

import iskallia.vault.config.UniqueGearConfig;
import iskallia.vault.core.util.WeightedList;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = UniqueGearConfig.class, remap = false)
public interface UniqueGearConfigAccessor {
    @Accessor
    Map<ResourceLocation, UniqueGearConfig.Entry> getRegistry();

    @Accessor
    Map<ResourceLocation, WeightedList<ResourceLocation>> getPools();
}
