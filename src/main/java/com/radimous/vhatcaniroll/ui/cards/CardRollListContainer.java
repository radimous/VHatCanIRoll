package com.radimous.vhatcaniroll.ui.cards;

import com.radimous.vhatcaniroll.logic.CardRolls;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.VerticalScrollClipContainer;
import iskallia.vault.client.gui.framework.screen.layout.ScreenLayout;
import iskallia.vault.client.gui.framework.spatial.Padding;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import iskallia.vault.init.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
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

        var gearPiece = new ItemStack(ModItems.BOOSTER_PACK);
//        // Label for the item name and level (GOLD if legendary, GREEN if greater, WHITE if common)
//        LabelElement<?> itemName = new LabelElement<>(
//            Spatials.positionXY(labelX, 5).width(this.innerWidth() - labelX).height(15), new TextComponent(gearPiece.getItem().getName(gearPiece).getString().toUpperCase())
//            .withStyle(ChatFormatting.UNDERLINE), LabelTextStyle.defaultStyle()
//        );
//        this.addElement(itemName);

        List<Component> modifiers = switch (screenType) {
            case "all" -> CardRolls.getAll();
            case "boosterPacks" -> CardRolls.getBoosterPackList();
            case "modifiers" -> CardRolls.getModifierList();
            case "conditions" -> CardRolls.getConditionsList();
            case "scalers" -> CardRolls.getScalerList();
            case "tasks" -> CardRolls.getTaskList();
            default -> List.of(new TextComponent("UNKNOWN CARD SCREEN TYPE").withStyle(ChatFormatting.RED));
        };

        if (modifiers == null || modifiers.isEmpty()) {
            LabelElement<?> labelelement = new LabelElement<>(
                Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15), new TextComponent(
                "Card roll config for " + gearPiece.getItem() + " not found"), LabelTextStyle.defaultStyle()
            );
            this.addElement(labelelement);
            return;
        }
        links.put("start", labelY);

        for (Component mc : modifiers) {

            if (mc instanceof TextComponent tc){ // try to make wrapped text
                var newTc = new TextComponent("");
                for (var sibling: tc.getSiblings()){
                    newTc.append(sibling);
                }
                var gtc = new TextComponent(tc.getText()).withStyle(tc.getStyle());
                LabelElement<?> gcl = new LabelElement<>(Spatials.positionXY(labelX, labelY), gtc, LabelTextStyle.defaultStyle());
                this.addElement(gcl);

                LabelElement<?> mcl = new LabelElement<>(
                    Spatials.positionXY(labelX + gcl.width(), labelY).width(this.innerWidth() - labelX - gcl.width()),
                    Spatials.width(this.innerWidth() - labelX * 2).height(9),
                    newTc, LabelTextStyle.wrap());
                this.addElement(mcl);
                if (tc.getStyle().getColor() != null &&tc.getStyle().getColor().getValue() == ChatFormatting.GREEN.getColor()) {
                    System.out.println("GREEN" + tc.getText() + this.getScroll());
                    links.put(tc.getText(), labelY);
                }
                labelY += Math.max(mcl.getTextStyle().calculateLines(newTc, mcl.width()) * 10, 10);
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
        visibleFraction -= ((float) (adjustedTargetLocation)/ adjustedEndLocation) * ((float) 8 / adjustedEndLocation);
        float targetScroll = (float) adjustedTargetLocation/ adjustedEndLocation + visibleFraction;
        targetScroll = Math.max(0, Math.min(1, targetScroll));
        this.setScroll(targetScroll);
    }

    public Map<String, Integer> getLinks() {
        return links;
    }
}