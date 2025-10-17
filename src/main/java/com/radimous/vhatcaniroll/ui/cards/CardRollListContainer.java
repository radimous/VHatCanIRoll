package com.radimous.vhatcaniroll.ui.cards;

import com.radimous.vhatcaniroll.logic.CardRolls;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CardRollListContainer extends VerticalScrollClipContainer<CardRollListContainer> implements InnerCardScreen {

    private final Map<String, Integer> links;
    public CardRollListContainer(ISpatial spatial, String screenType) {
        super(spatial, Padding.ZERO, ScreenTextures.INSET_BLACK_BACKGROUND);
        links = new LinkedHashMap<>(); // insertion order
        int labelX = 9;
        int labelY = 10;

        List<Component> modifiers = switch (screenType) {
            case "all" -> CardRolls.getAll();
            case "boosterPacks" -> CardRolls.getBoosterPackList();
            case "modifiers" -> CardRolls.getModifierList();
            case "conditions" -> CardRolls.getConditionsList();
            case "scalers" -> CardRolls.getScalerList();
            case "tasks" -> CardRolls.getTaskList();
            default -> List.of(new TextComponent("UNKNOWN CARD SCREEN TYPE").withStyle(ChatFormatting.RED));
        };

        if (modifiers.isEmpty()) {
            LabelElement<?> labelelement = new LabelElement<>(
                Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15), new TextComponent(
                "No "+screenType+" info "), LabelTextStyle.defaultStyle()
            );
            this.addElement(labelelement);
            return;
        }
        links.put("start", labelY);

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
                if (tc.getStyle().getColor() != null && tc.getStyle().getColor().getValue() == ChatFormatting.GREEN.getColor()) {
                    links.put(tc.getText(), labelY);
                }
                labelY += Math.max(mcl.getTextStyle().calculateLines(newTc, mcl.width() - whiteSpaceWidth) * 10, 10);
            } else {
                LabelElement<?> labelelement = new LabelElement<>(
                    Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15), mc, LabelTextStyle.defaultStyle()
                );
                this.addElement(labelelement);
                labelY += 10;
            }
        }
        links.put("end", labelY - 10);
    }
    public float getScroll() {
        return this.verticalScrollBarElement.getValue();
    }

    public void setScroll(float scroll) {
        this.verticalScrollBarElement.setValue(scroll);
    }

    @Override
    public InnerCardScreen create(ISpatial spatial, String screenType) {
        return new CardRollListContainer(spatial, screenType);
    }

    public void scrollToLink(String target) {
        // dark magic
        var targetLocation = links.get(target);
        var endLocation = links.get("end");
        if (targetLocation == null || endLocation == null) {
            return;
        }
        var adjustedTargetLocation = Math.max(0,targetLocation - 6) ;
        var adjustedEndLocation = Math.max(endLocation, 1); // prevent /0
        float visibleFraction = ((float) (adjustedTargetLocation)/ adjustedEndLocation) * ((float) height() / adjustedEndLocation);
//        visibleFraction -= ((float) (adjustedTargetLocation)/ adjustedEndLocation) * ((float) 8 / adjustedEndLocation);
        float targetScroll = (float) adjustedTargetLocation/ adjustedEndLocation + visibleFraction;
        targetScroll = Math.max(0, Math.min(1, targetScroll));
        this.setScroll(targetScroll);
    }

    public Map<String, Integer> getLinks() {
        return links;
    }
}