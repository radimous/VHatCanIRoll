package com.radimous.vhatcaniroll.mixin;

import com.radimous.vhatcaniroll.Config;
import com.radimous.vhatcaniroll.QOLHuntersCompat;
import com.radimous.vhatcaniroll.ui.gear.GearModifierScreen;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.ButtonElement;
import iskallia.vault.client.gui.framework.element.FakeItemSlotElement;
import iskallia.vault.client.gui.framework.render.TooltipDirection;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.screen.player.AbstractSkillTabElementContainerScreen;
import iskallia.vault.client.gui.screen.player.StatisticsElementContainerScreen;
import iskallia.vault.container.StatisticsTabContainer;
import iskallia.vault.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = StatisticsElementContainerScreen.class, remap = false)
public class StatisticsElementContainerScreenMixin extends AbstractSkillTabElementContainerScreen<StatisticsTabContainer> {
    public StatisticsElementContainerScreenMixin(StatisticsTabContainer container, Inventory inventory, Component title, IElementRenderer elementRenderer) {
        super(container, inventory, title, elementRenderer);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void addGearButton(StatisticsTabContainer container, Inventory inventory, Component title, CallbackInfo ci) {
        if (!Config.VAULT_SCREEN_BUTTON.get()){
            return;
        }

        // TODO: figure out how to add button like the quest button, not this ugly shit

        QOLHuntersCompat.resolveQOLHuntersButtonConflict();

        // add blank button to vault screen
        this.addElement(
            new ButtonElement<>(Spatials.positionXY(-3, 3), ScreenTextures.BUTTON_EMPTY_16_TEXTURES, () -> {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                Minecraft.getInstance().setScreen(new GearModifierScreen());
            }).layout(
                    (screen, gui, parent, world) -> world.width(21).height(21).translateX(gui.right() + Config.BUTTON_X.get()).translateY(this.getTabContentSpatial().bottom() + Config.BUTTON_Y.get())
                )
                .tooltip((tooltipRenderer, poseStack, mouseX, mouseY, tooltipFlag) -> {
                    tooltipRenderer.renderTooltip(poseStack, List.of(new TextComponent("Gear modifiers")), mouseX, mouseY, ItemStack.EMPTY, TooltipDirection.RIGHT);
                    return false;
                })
        );
        // add chestplate icon to it
        ItemStack chestplateStack = new ItemStack(ModItems.CHESTPLATE);
        this.addElement(
            new FakeItemSlotElement<>(Spatials.positionXY(-3, 3), () -> chestplateStack, () -> false, ScreenTextures.EMPTY, ScreenTextures.EMPTY
            ).layout(
                    (screen, gui, parent, world) -> world.width(21).height(21).translateX(gui.right() + Config.BUTTON_X.get()).translateY(this.getTabContentSpatial().bottom() + Config.BUTTON_Y.get())
                )
                .tooltip((tooltipRenderer, poseStack, mouseX, mouseY, tooltipFlag) -> {
                    tooltipRenderer.renderTooltip(poseStack, List.of(new TextComponent("Gear modifiers")), mouseX, mouseY, ItemStack.EMPTY, TooltipDirection.RIGHT);
                    return false;
                })
        );
    }

    @Shadow
    @Override
    public int getTabIndex() {
        return 0;
    }

    @Shadow
    @Override
    public MutableComponent getTabTitle() {
        return null;
    }
}
