package com.radimous.vhatcaniroll.mixin.accessors;

import iskallia.vault.gear.attribute.config.DoubleAttributeGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = DoubleAttributeGenerator.Range.class, remap = false)
public interface DoubleRangeAccessor {
    @Accessor
    double getMin();

    @Accessor
    double getMax();
}
