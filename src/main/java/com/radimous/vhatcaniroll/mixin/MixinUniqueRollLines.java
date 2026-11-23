package com.radimous.vhatcaniroll.mixin;

import com.radimous.vhatcaniroll.Config;
import com.radimous.vhatcaniroll.logic.ModifierCategory;
import com.radimous.vhatcaniroll.logic.UniqueModifiers;
import iskallia.vault.client.gui.overlay.VaultBarOverlay;
import iskallia.vault.config.UniqueGearConfig;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.tooltip.UniqueRollLines;
import iskallia.vault.init.ModConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(value = UniqueRollLines.class, remap = false)
public class MixinUniqueRollLines {
    @Inject(method = "forUnique", at = @At("HEAD"), cancellable = true)
    private static void replaceTooltip(ResourceLocation uniqueId, int vaultLevel, LogicalSide side, CallbackInfoReturnable<List<Component>> cir) {
        if (!Config.REPLACE_UNIQUE_CODEX_TOOLTIP.get()) return;
        int lvl = Math.min(VaultBarOverlay.vaultLevel, 100);
        Optional<UniqueGearConfig.Entry> entryOpt = ModConfigs.UNIQUE_GEAR.getEntry(uniqueId);
        if (entryOpt.isEmpty()) {
            return;
        }

        UniqueGearConfig.Entry value = entryOpt.get();
        Map<UniqueGearConfig.AffixTargetType, List<ResourceLocation>> modifierIdentifiers = value.getModifierIdentifiers();
        if (modifierIdentifiers == null) {
            return;
        }

        var modifiers = UniqueModifiers.getUniqueModifierList(lvl, ModifierCategory.NORMAL, modifierIdentifiers, false, false);
        modifiers.add(0, new TextComponent(value.getName()).withStyle(Style.EMPTY.withColor(VaultGearRarity.UNIQUE.getColor()))
                .append(new TextComponent( " - lvl " + lvl).withStyle(ChatFormatting.GRAY)));
        cir.setReturnValue(modifiers);

    }
}
