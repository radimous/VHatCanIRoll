package com.radimous.vhatcaniroll.mixin;

import iskallia.vault.client.gui.screen.block.VaultSealerScreen;
import iskallia.vault.integration.jei.IntegrationJEI;
import iskallia.vault.integration.jei.RemoveJEIContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = IntegrationJEI.class, remap = false)
public class MixinIntegrationJEI {
    @Inject(method = "registerGuiHandlers", at = @At("TAIL"))
    private void hideJeiInSealerScreen(IGuiHandlerRegistration registration, CallbackInfo ci) {
        registration.addGenericGuiContainerHandler(VaultSealerScreen.class, new RemoveJEIContainerHandler<>());
    }
}
