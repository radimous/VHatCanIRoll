package com.radimous.vhatcaniroll.ui;

import com.radimous.vhatcaniroll.logic.ModifierCategory;
import iskallia.vault.client.gui.framework.element.ContainerElement;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.spi.ILayoutStrategy;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;

public class HelpContainer extends ContainerElement<HelpContainer> {
    public HelpContainer(ISpatial spatial) {
        super(spatial);
        this.setVisible(false); // Hide by default

        var tabLabel = new LabelElement<>(
            Spatials.positionXY(336, 16).width(16).height(16),
            new TextComponent("TAB").withStyle(ChatFormatting.GOLD),
            LabelTextStyle.shadow()
        ).layout(this.translateWorldSpatial());
        this.addElement(tabLabel);

        var shiftTabLabel = new LabelElement<>(
            Spatials.positionXY(-60, 16).width(16).height(16),
            new TextComponent("SHIFT + TAB").withStyle(ChatFormatting.GOLD),
            LabelTextStyle.shadow()
        ).layout(this.translateWorldSpatial());
        this.addElement(shiftTabLabel);

        var arrows = new LabelElement<>(
            Spatials.positionXY(260, 54).width(16).height(16),
            new TextComponent("← ").withStyle(ChatFormatting.GOLD)
                .append(new TextComponent("scroll").withStyle(ChatFormatting.BLUE))
                .append(new TextComponent(" →").withStyle(ChatFormatting.GOLD)),
            LabelTextStyle.shadow()
        ).layout(this.translateWorldSpatial());
        this.addElement(arrows);

        var vimArrows = new LabelElement<>(
            Spatials.positionXY(262, 64).width(16).height(16),
            new TextComponent("h ").withStyle(ChatFormatting.GOLD)
                .append(new TextComponent("wheel").withStyle(ChatFormatting.BLUE))
                .append(new TextComponent("  l").withStyle(ChatFormatting.GOLD)),
            LabelTextStyle.shadow()
        ).layout(this.translateWorldSpatial());
        this.addElement(vimArrows);

        var ctrlLabel = new LabelElement<>(
            Spatials.positionXY(340, 38).width(16).height(16),
            new TextComponent("CTRL").withStyle(ChatFormatting.GOLD),
            LabelTextStyle.shadow()
        ).layout(this.translateWorldSpatial());
        this.addElement(ctrlLabel);

        var categoryLabelNormal = new LabelElement<>(
            Spatials.positionXY(350, 52).width(16).height(16),
            new TextComponent(ModifierCategory.NORMAL.name()).withStyle(ModifierCategory.NORMAL.getStyle()),
            LabelTextStyle.shadow()
        ).layout(this.translateWorldSpatial());
        this.addElement(categoryLabelNormal);

        var categoryLabelGreater = new LabelElement<>(
            Spatials.positionXY(350, 62).width(16).height(16),
            new TextComponent(ModifierCategory.GREATER.name()).withStyle(ModifierCategory.GREATER.getStyle()),
            LabelTextStyle.shadow()
        ).layout(this.translateWorldSpatial());
        this.addElement(categoryLabelGreater);

        var categoryLabelLegendary = new LabelElement<>(
            Spatials.positionXY(350, 72).width(16).height(16),
            new TextComponent(ModifierCategory.LEGENDARY.name()).withStyle(ModifierCategory.LEGENDARY.getStyle()),
            LabelTextStyle.shadow()
        ).layout(this.translateWorldSpatial());
        this.addElement(categoryLabelLegendary);

        var upLabel = new LabelElement<>(
            Spatials.positionXY(340, 150).width(16).height(16),
            new TextComponent("↑ k").withStyle(ChatFormatting.GOLD),
            LabelTextStyle.shadow()
        ).layout(this.translateWorldSpatial());
        this.addElement(upLabel);
        var downLabel = new LabelElement<>(
            Spatials.positionXY(340, 164).width(16).height(16),
            new TextComponent("↓ j").withStyle(ChatFormatting.GOLD),
            LabelTextStyle.shadow()
        ).layout(this.translateWorldSpatial());
        this.addElement(downLabel);


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
        int labelY = 120;
        for (String s : array) {
            var textLabel = new LabelElement<>(
                Spatials.positionXY(-100, labelY).width(20).height(15),
                new TextComponent(s).withStyle(ChatFormatting.GOLD), LabelTextStyle.shadow()
            ).layout(this.translateWorldSpatial());
            this.addElement(textLabel);
            labelY += 10;
        }


    }
    private ILayoutStrategy translateWorldSpatial() {
        return (screen, gui, parent, world) -> world.translateXY(gui);
    }
}
