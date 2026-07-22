package com.radimous.vhatcaniroll.ui;

import iskallia.vault.client.gui.framework.element.ContainerElement;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.spi.IElement;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.List;
import java.util.function.Consumer;

public class UIUtil {
    public static int addWrappedToContainer(List<Component> components, int labelX, int labelY, int innerWidth, Consumer<IElement> elemAdder){
        for (Component mc : components) {

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
                        Spatials.positionXY(labelX + whiteSpaceWidth , labelY).width(innerWidth - labelX - whiteSpaceWidth),
                        Spatials.width(innerWidth - labelX * 2 - whiteSpaceWidth).height(9),
                        newTc, LabelTextStyle.wrap());
//                this.addElement(mcl);
                elemAdder.accept(mcl);
                labelY += Math.max(mcl.getTextStyle().calculateLines(newTc, mcl.width() - whiteSpaceWidth) * 10, 10);
            } else {
                LabelElement<?> labelelement = new LabelElement<>(
                        Spatials.positionXY(labelX, labelY).width(innerWidth - labelX).height(15), mc, LabelTextStyle.defaultStyle()
                );
                elemAdder.accept(labelelement);
//                this.addElement(labelelement);
                labelY += 10;
            }
        }
        return labelY;
    }

}
