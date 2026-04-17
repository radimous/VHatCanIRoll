package com.radimous.vhatcaniroll.mixin;

import com.radimous.vhatcaniroll.Config;
import com.radimous.vhatcaniroll.ui.artisan.ArtisanReplacement;
import iskallia.vault.client.gui.framework.element.ModifierListElement;
import iskallia.vault.client.gui.framework.element.VerticalScrollClipContainer;
import iskallia.vault.client.gui.framework.screen.layout.ScreenLayout;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.config.gear.VaultGearTierConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ModifierListElement.class, remap = false)
public abstract class MixinModifierListElement<E extends ModifierListElement<E>> extends VerticalScrollClipContainer<E> {
    private MixinModifierListElement(ISpatial spatial) {
        super(spatial);
    }

    @Shadow protected abstract int getItemLevel(ItemStack stack);

    @Inject(method = "updateModifiers", at = @At(value = "INVOKE", target = "Liskallia/vault/client/gui/framework/element/ModifierListElement;addImplicitSection(Liskallia/vault/config/gear/VaultGearTierConfig;II)I"), cancellable = true)
    private void replace(ItemStack gearStack, CallbackInfo ci){

        if (!Config.REPLACE_ARTISAN_MODIFIER_LIST.get()) {
            return;
        }
        VaultGearTierConfig config = VaultGearTierConfig.getConfig(gearStack).orElse(null);
        if (config == null) {
            return;
        }
        int itemLevel = this.getItemLevel(gearStack);
        ci.cancel();
        ArtisanReplacement.addModifierList(itemLevel, config, (ModifierListElement<?>) (Object) this, this.innerContainerElement);
        ScreenLayout.requestLayout();
    }

    @Inject(method = "getCalculatedWidth", at = @At("RETURN"), cancellable = true)
    private void adjustWidth(CallbackInfoReturnable<Integer> cir) {
        int availableWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth() - this.left() - 14 /* frame width */;
        if (availableWidth < Config.ARTISAN_MODIFIER_LIST_WIDTH.get()) {
            cir.setReturnValue(availableWidth);
        } else {
            cir.setReturnValue(Config.ARTISAN_MODIFIER_LIST_WIDTH.get());
        }
    }
}
