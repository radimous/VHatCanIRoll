package com.radimous.vhatcaniroll.ui;

import com.radimous.vhatcaniroll.logic.ModifierCategory;
import iskallia.vault.client.gui.framework.element.ContainerElement;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;

public class HelpContainer extends ContainerElement<HelpContainer> {
    public HelpContainer(ISpatial spatial, ISpatial parentSpatial) {
        // TODO: make this size agnostic
        super(spatial);
        this.setVisible(false); // Hide by default

        int labelX = 9;
        int labelY = 120;

        var tabLabel = new LabelElement<>(
            Spatials.positionXY(490, 24).width(16).height(16),
            new TextComponent("TAB").withStyle(ChatFormatting.GOLD),
            LabelTextStyle.shadow()
        );
        this.addElement(tabLabel);

        var shiftTabLabel = new LabelElement<>(
            Spatials.positionXY(84, 24).width(16).height(16),
            new TextComponent("SHIFT + TAB").withStyle(ChatFormatting.GOLD),
            LabelTextStyle.shadow()
        );

        var arrows = new LabelElement<>(
            Spatials.positionXY(408, 62).width(16).height(16),
            new TextComponent("← ").withStyle(ChatFormatting.GOLD)
                .append(new TextComponent("scroll").withStyle(ChatFormatting.BLUE))
                .append(new TextComponent(" →").withStyle(ChatFormatting.GOLD)),
            LabelTextStyle.shadow()
        );
        this.addElement(arrows);

        var vimArrows = new LabelElement<>(
            Spatials.positionXY(410, 72).width(16).height(16),
            new TextComponent("h ").withStyle(ChatFormatting.GOLD)
                .append(new TextComponent("wheel").withStyle(ChatFormatting.BLUE))
                .append(new TextComponent("  l").withStyle(ChatFormatting.GOLD)),
            LabelTextStyle.shadow()
        );
        this.addElement(vimArrows);

        var ctrlLabel = new LabelElement<>(
            Spatials.positionXY(494, 46).width(16).height(16),
            new TextComponent("CTRL").withStyle(ChatFormatting.GOLD),
            LabelTextStyle.shadow()
        );
        this.addElement(ctrlLabel);

        var categoryLabelNormal = new LabelElement<>(
            Spatials.positionXY(510, 56).width(16).height(16),
            new TextComponent(ModifierCategory.NORMAL.name()).withStyle(ModifierCategory.NORMAL.getStyle()),
            LabelTextStyle.shadow()
        );
        this.addElement(categoryLabelNormal);

        var categoryLabelGreater = new LabelElement<>(
            Spatials.positionXY(510, 66).width(16).height(16),
            new TextComponent(ModifierCategory.GREATER.name()).withStyle(ModifierCategory.GREATER.getStyle()),
            LabelTextStyle.shadow()
        );
        this.addElement(categoryLabelGreater);

        var categoryLabelLegendary = new LabelElement<>(
            Spatials.positionXY(510, 76).width(16).height(16),
            new TextComponent(ModifierCategory.LEGENDARY.name()).withStyle(ModifierCategory.LEGENDARY.getStyle()),
            LabelTextStyle.shadow()
        );
        this.addElement(categoryLabelLegendary);

        var upLabel = new LabelElement<>(
            Spatials.positionXY(494, 150).width(16).height(16),
            new TextComponent("↑ k").withStyle(ChatFormatting.GOLD),
            LabelTextStyle.shadow()
        );
        this.addElement(upLabel);
        var downLabel = new LabelElement<>(
            Spatials.positionXY(494, 164).width(16).height(16),
            new TextComponent("↓ j").withStyle(ChatFormatting.GOLD),
            LabelTextStyle.shadow()
        );
        this.addElement(downLabel);

        this.addElement(shiftTabLabel);


        String text = """
            Colored triangles
            represent groups
            of attributes.
            
            If 2 attributes are
            in the same group,
            they can't be
            rolled together.
            """;

        var array = text.split("\n");
        for (String s : array) {
            var textLabel = new LabelElement<>(
                Spatials.positionXY(labelX, labelY).width(20).height(15),
                new TextComponent(s).withStyle(ChatFormatting.GOLD), LabelTextStyle.shadow()
            );
            this.addElement(textLabel);
            labelY += 10;
        }


    }
}
