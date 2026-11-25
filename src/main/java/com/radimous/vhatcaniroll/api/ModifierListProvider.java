package com.radimous.vhatcaniroll.api;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Supplies the modifier lines for gear whose rolls VHatCanIRoll cannot read from a gear config.
 * Register one over IMC with the method {@code register_modifier_list}.
 */
@FunctionalInterface
public interface ModifierListProvider {
    /**
     * The lines to list for {@code stack} at {@code level}, in the same form as the built-in list (a bold
     * header line, then one line per modifier), or {@code null} to leave the page to VHatCanIRoll.
     */
    @Nullable
    List<Component> modifiers(ItemStack stack, int level, boolean mythic);
}
