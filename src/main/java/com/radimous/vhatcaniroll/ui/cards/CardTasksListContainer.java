package com.radimous.vhatcaniroll.ui.cards;

import com.radimous.vhatcaniroll.logic.CardRolls;
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

public class CardTasksListContainer extends VerticalScrollClipContainer<CardTasksListContainer> implements InnerCardScreen {

    public CardTasksListContainer(ISpatial spatial) {
        super(spatial, Padding.ZERO, ScreenTextures.INSET_BLACK_BACKGROUND);
        int labelX = 9;
        int labelY = 10;

        List<Component> modifiers = CardRolls.getTaskList();

        if (modifiers.isEmpty()) {
            LabelElement<?> labelelement = new LabelElement<>(
                Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15), new TextComponent(
                "No card tasks found "), LabelTextStyle.defaultStyle()
            );
            this.addElement(labelelement);
            return;
        }

        for (Component mc : modifiers) {

            if (mc instanceof TextComponent tc){ // try to make wrapped text
                String stripped = tc.getText().stripLeading();
                String removed = tc.getText().substring(0, tc.getText().length() - stripped.length());
                int whiteSpaceWidth = Minecraft.getInstance().font.width(removed);
                var newTc = new TextComponent(stripped).withStyle(tc.getStyle());
                for (var sibling: tc.getSiblings()){
                    if (sibling.getString().equals(tc.getText())) {
                        continue;
                    }
                    newTc.append(sibling);
                }

                LabelElement<?> mcl = new LabelElement<>(
                    Spatials.positionXY(labelX + whiteSpaceWidth , labelY).width(this.innerWidth() - labelX - whiteSpaceWidth),
                    Spatials.width(this.innerWidth() - labelX * 2 - whiteSpaceWidth).height(9),
                    newTc, LabelTextStyle.wrap());
                this.addElement(mcl);
                labelY += Math.max(mcl.getTextStyle().calculateLines(newTc, mcl.width() - whiteSpaceWidth) * 10, 10);
            } else {
                LabelElement<?> labelelement = new LabelElement<>(
                    Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15), mc, LabelTextStyle.defaultStyle()
                );
                this.addElement(labelelement);
                labelY += 10;
            }
        }
    }
    public float getScroll() {
        return this.verticalScrollBarElement.getValue();
    }

    public void setScroll(float scroll) {
        this.verticalScrollBarElement.setValue(scroll);
    }

    @Override
    public InnerCardScreen create(ISpatial spatial) {
        return new CardTasksListContainer(spatial);
    }
}