package com.radimous.vhatcaniroll.mixin.accessors;

import iskallia.vault.gear.attribute.config.FloatAttributeGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = FloatAttributeGenerator.Range.class, remap = false)
public interface FloatRangeAccessor {
    @Accessor
    float getMin();

    @Accessor
    float getMax();
}
