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
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.List;

public class BoosterPackListContainer extends VerticalScrollClipContainer<BoosterPackListContainer> implements InnerCardScreen {

    public BoosterPackListContainer(ISpatial spatial) {
        super(spatial, Padding.ZERO, ScreenTextures.INSET_BLACK_BACKGROUND);
        int labelX = 9;
        int labelY = 10;

        List<Component> modifiers = CardRolls.getBoosterPackList();

        if (modifiers.isEmpty()) {
            LabelElement<?> labelelement = new LabelElement<>(
                Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15), new TextComponent(
                "No booster packs found"), LabelTextStyle.defaultStyle()
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
        return new BoosterPackListContainer(spatial);
    }
}