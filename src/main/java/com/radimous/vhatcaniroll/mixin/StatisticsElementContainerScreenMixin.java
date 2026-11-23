package com.radimous.vhatcaniroll.mixin;

import com.radimous.vhatcaniroll.Config;
import com.radimous.vhatcaniroll.ui.mainhud.MainHud;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.screen.player.AbstractSkillTabElementContainerScreen;
import iskallia.vault.client.gui.screen.player.StatisticsElementContainerScreen;
import iskallia.vault.container.StatisticsTabContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
        this.addElement(MainHud.getOpenButton(this.getTabContentSpatial()));
        this.addElement(MainHud.getChestplateStack(this.getTabContentSpatial()));
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
