package com.radimous.vhatcaniroll.mixin;

import com.radimous.vhatcaniroll.ui.artisan.ArtisanButton;
import iskallia.vault.client.gui.framework.element.ModifierListElement;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.render.spi.ITooltipRendererFactory;
import iskallia.vault.client.gui.framework.screen.AbstractElementContainerScreen;
import iskallia.vault.client.gui.screen.block.VaultArtisanStationScreen;
import iskallia.vault.container.VaultArtisanStationContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VaultArtisanStationScreen.class, remap = false)
public abstract class MixinArtisanStationScreen extends AbstractElementContainerScreen<VaultArtisanStationContainer> {
    @Shadow private ModifierListElement<?> modifierListElement;

    private MixinArtisanStationScreen(VaultArtisanStationContainer container, Inventory inventory, Component title,
                                     IElementRenderer elementRenderer,
                                     ITooltipRendererFactory<AbstractElementContainerScreen<VaultArtisanStationContainer>> tooltipRendererFactory) {
        super(container, inventory, title, elementRenderer, tooltipRendererFactory);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void addToggleButton(VaultArtisanStationContainer container, Inventory inventory, Component title, CallbackInfo ci){
        this.addElement(ArtisanButton.create((VaultArtisanStationScreen) (Object) this, this.modifierListElement));
    }
}
