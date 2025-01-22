package com.radimous.vhatcaniroll.mixin;

import iskallia.vault.gear.attribute.ability.AbilityFloatValueAttribute;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbilityFloatValueAttribute.Reader.class)
public interface AbilityFloatValueAttributeReaderInvoker {
    @Invoker
    MutableComponent invokeFormatAbilityName(String abilityKey);
}
