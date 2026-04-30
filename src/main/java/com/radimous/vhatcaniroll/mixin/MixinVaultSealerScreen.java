package com.radimous.vhatcaniroll.mixin;

import com.radimous.vhatcaniroll.Config;
import com.radimous.vhatcaniroll.ui.sealer.SealerButton;
import com.radimous.vhatcaniroll.ui.sealer.SealerModifierListElement;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.NineSliceElement;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.render.spi.ITooltipRendererFactory;
import iskallia.vault.client.gui.framework.screen.AbstractElementContainerScreen;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import iskallia.vault.client.gui.screen.block.VaultSealerScreen;
import iskallia.vault.container.VaultSealerContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VaultSealerScreen.class, remap = false)
public abstract class MixinVaultSealerScreen extends AbstractElementContainerScreen<VaultSealerContainer>  {
    @Unique private SealerModifierListElement<?> vHatCanIRoll$modifierListElement;
    @Unique private NineSliceElement<?> vHatCanIRoll$modifierPanelBackground;
    @Unique private ItemStack vHatCanIRoll$lastGearStack = ItemStack.EMPTY;

    public MixinVaultSealerScreen(VaultSealerContainer container, Inventory inventory, Component title,
                                  IElementRenderer elementRenderer,
                                  ITooltipRendererFactory<AbstractElementContainerScreen<VaultSealerContainer>> tooltipRendererFactory) {
        super(container, inventory, title, elementRenderer, tooltipRendererFactory);
    }


    @Inject(method = "<init>", at = @At("RETURN"))
    private void addModifierList(VaultSealerContainer container, Inventory inventory, Component title, CallbackInfo ci) {
        if (!Config.SEALER_MODIFIER_LIST.get()) return;

        this.vHatCanIRoll$modifierListElement = new SealerModifierListElement<>(Spatials.positionXY(187, 18).size(100, 159));
        this.vHatCanIRoll$modifierListElement.layout((screen, gui, parent, world) -> {
            world.translateXY(gui);
            world.width(this.vHatCanIRoll$modifierListElement.getCalculatedWidth());
        });
        this.vHatCanIRoll$modifierPanelBackground =
            new NineSliceElement<>(Spatials.positionXY(180, 0).size(100, 185), ScreenTextures.DEFAULT_WINDOW_BACKGROUND);
        this.vHatCanIRoll$modifierPanelBackground.layout((screen, gui, parent, world) -> {
            world.translateXY(gui);
            world.width(this.vHatCanIRoll$modifierListElement.getCalculatedWidth() + 8 + 6);
        });
        this.vHatCanIRoll$modifierPanelBackground.setVisible(!this.getMenu().getGearInputSlot().getItem().isEmpty());
        this.vHatCanIRoll$modifierListElement.setVisible(!(this.getMenu().getGearInputSlot().getItem().isEmpty()));
        this.addElement(this.vHatCanIRoll$modifierPanelBackground);
        this.addElement((new LabelElement(Spatials.positionXY(188, 7), (new TextComponent("Available Modifiers")).withStyle(Style.EMPTY.withColor(-12632257)), LabelTextStyle.defaultStyle()) {
            public boolean isVisible() {
                return vHatCanIRoll$modifierListElement.isVisible();
            }
        }).layout(this.translateWorldSpatial()));
        this.addElement(this.vHatCanIRoll$modifierListElement);
        this.addElement(SealerButton.create((VaultSealerScreen) (Object) this, this.vHatCanIRoll$modifierListElement));
    }

    @Inject(method = "containerTick", at = @At("TAIL"), remap = true)
    private void tick(CallbackInfo ci) {
        if (!Config.SEALER_MODIFIER_LIST.get()) return;

        ItemStack currentGearStack = this.getMenu().getGearInputSlot().getItem();
        if (currentGearStack.isEmpty() && !this.getMenu().getOutputSlot().getItem().isEmpty()) {
            currentGearStack = this.getMenu().getOutputSlot().getItem();
        }
        if (!ItemStack.isSameItemSameTags(this.vHatCanIRoll$lastGearStack, currentGearStack)) {
            this.vHatCanIRoll$lastGearStack = currentGearStack.copy();
            this.vHatCanIRoll$modifierListElement.setVisible(!currentGearStack.isEmpty());
            this.vHatCanIRoll$modifierPanelBackground.setVisible(!currentGearStack.isEmpty());
            this.vHatCanIRoll$modifierListElement.updateModifiers(currentGearStack);
        }
    }
}
