package com.radimous.vhatcaniroll.ui.mainhud;

import com.radimous.vhatcaniroll.Config;
import com.radimous.vhatcaniroll.ui.gear.GearModifierScreen;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.FakeItemSlotElement;
import iskallia.vault.client.gui.framework.element.NineSliceButtonElement;
import iskallia.vault.client.gui.framework.element.spi.IElement;
import iskallia.vault.client.gui.framework.render.TooltipDirection;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MainHud {

    public static IElement getChestplateStack(ISpatial tabContentSpatial) {
        ItemStack chestplateStack = new ItemStack(ModItems.CHESTPLATE);

        return new FakeItemSlotElement<>(Spatials.positionXY(-3, 3), () -> chestplateStack, () -> false, ScreenTextures.EMPTY, ScreenTextures.EMPTY
        ).layout(
                (screen, gui, parent, world) -> world.width(20).height(20).translateX(
                        gui.right() + 25 + Config.BUTTON_X.get()).translateY(
                        tabContentSpatial.bottom() + 15 + Config.BUTTON_Y.get()))
                .tooltip((tooltipRenderer, poseStack, mouseX, mouseY, tooltipFlag) -> {
                    tooltipRenderer.renderTooltip(poseStack, List.of(new TextComponent("Gear modifiers")), mouseX, mouseY, ItemStack.EMPTY, TooltipDirection.RIGHT);
                    return false;
                });
    }

    public static IElement getOpenButton(ISpatial tabContentSpatial) {
        return new NineSliceButtonElement<>(Spatials.positionXY(-3, 3), ScreenTextures.BUTTON_EMPTY_GRAY_TEXTURES, () -> {
            Minecraft.getInstance().setScreen(new GearModifierScreen());
        }).layout((screen, gui, parent, world) -> world.width(20).height(20).translateX(gui.right() + 24 + Config.BUTTON_X.get()).translateY(
                        tabContentSpatial.bottom() + 14 + Config.BUTTON_Y.get()))
                .tooltip((tooltipRenderer, poseStack, mouseX, mouseY, tooltipFlag) -> {
                    tooltipRenderer.renderTooltip(poseStack, List.of(new TextComponent("Gear modifiers")), mouseX, mouseY, ItemStack.EMPTY, TooltipDirection.RIGHT);
                    return false;
                });
    }
}
