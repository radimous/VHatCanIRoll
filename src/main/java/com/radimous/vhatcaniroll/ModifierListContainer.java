package com.radimous.vhatcaniroll;

import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.VerticalScrollClipContainer;
import iskallia.vault.client.gui.framework.spatial.Padding;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import iskallia.vault.config.gear.VaultGearTierConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;

import java.util.Optional;

public class ModifierListContainer extends VerticalScrollClipContainer<ModifierListContainer> {

    public ModifierListContainer(ISpatial spatial, GearModifierScreen parent) {
        super(spatial, Padding.ZERO, ScreenTextures.INSET_BLACK_BACKGROUND);
        int i = 20;
        int k = 9;
        boolean legendary = parent.isLegendary();
        Optional<VaultGearTierConfig> optCfg = VaultGearTierConfig.getConfig(parent.getCurrGear());
        LabelElement<?> itemName = new LabelElement<>(
            Spatials.positionXY(k, 5).width(this.innerWidth() - k).height(15), new TextComponent(
            parent.getCurrGear().getItem().toString().toUpperCase() + " - LVL " + parent.getCurrLvl())
            .withStyle(ChatFormatting.UNDERLINE).withStyle(legendary ? ChatFormatting.GOLD : ChatFormatting.WHITE), LabelTextStyle.defaultStyle()
        );
        this.addElement(itemName);
        if (optCfg.isPresent()) {
            VaultGearTierConfig cfg = optCfg.get();
            for (var modifier : Helper.getModifierList(parent.getCurrLvl(), cfg, legendary)) {
                LabelElement<?> labelelement = new LabelElement<>(
                    Spatials.positionXY(k, i).width(this.innerWidth() - k).height(15), modifier, LabelTextStyle.defaultStyle()
                );
                this.addElement(labelelement);
                i += 10;
            }
        } else {
            LabelElement<?> labelelement = new LabelElement<>(
                Spatials.positionXY(k, i).width(this.innerWidth() - k).height(15), new TextComponent(
                parent.getCurrGear().getItem() + " not found"), LabelTextStyle.defaultStyle()
            );
            this.addElement(labelelement);
        }
    }
    public float getScroll() {
        return this.verticalScrollBarElement.getValue();
    }

    public void setScroll(float scroll) {
        this.verticalScrollBarElement.setValue(scroll);
    }
}