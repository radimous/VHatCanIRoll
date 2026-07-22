package com.radimous.vhatcaniroll.ui.cards.inner;

import com.radimous.vhatcaniroll.logic.CardRolls;
import com.radimous.vhatcaniroll.ui.UIUtil;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.VerticalScrollClipContainer;
import iskallia.vault.client.gui.framework.spatial.Padding;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.List;

public class CardModifierListContainer extends VerticalScrollClipContainer<CardModifierListContainer> implements InnerCardScreen {

    String modifierPool;
    public CardModifierListContainer(ISpatial spatial, String modifierPool) {
        super(spatial, Padding.ZERO, ScreenTextures.INSET_BLACK_BACKGROUND);
        int labelX = 9;
        int labelY = 10;
        this.modifierPool = modifierPool;


        List<Component> modifiers;
        if (modifierPool == null) {
            modifiers = List.of(new TextComponent("Select card modifier pool from the list on the right => ").withStyle(ChatFormatting.YELLOW));
        } else {
            modifiers = CardRolls.getModifierList(modifierPool);
        }

        if (modifiers.isEmpty()) {
            LabelElement<?> labelelement = new LabelElement<>(
                Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15), new TextComponent(
                "No card modifiers found "), LabelTextStyle.defaultStyle()
            );
            this.addElement(labelelement);
            return;
        }

        UIUtil.addWrappedToContainer(modifiers, labelX, labelY, this.innerWidth(), this::addElement);
    }
    public float getScroll() {
        return this.verticalScrollBarElement.getValue();
    }

    public void setScroll(float scroll) {
        this.verticalScrollBarElement.setValue(scroll);
    }

    @Override
    public InnerCardScreen create(ISpatial spatial) {
        return new CardModifierListContainer(spatial, modifierPool);
    }
}