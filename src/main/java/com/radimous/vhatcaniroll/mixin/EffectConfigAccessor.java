package com.radimous.vhatcaniroll.mixin;

import iskallia.vault.gear.attribute.custom.effect.EffectGearAttribute;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = EffectGearAttribute.Config.class, remap = false)
public interface EffectConfigAccessor {
    @Accessor
    int getAmplifier();
    @Accessor
    ResourceLocation getEffectKey();
}
