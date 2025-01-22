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

        LabelElement<?> tabLabel = new LabelElement<>(
            Spatials.positionXY(336, 16).width(16).height(16),
            new TextComponent("TAB").withStyle(ChatFormatting.GOLD),
            LabelTextStyle.shadow()
        ).layout(this.translateWorldSpatial());
        this.addElement(tabLabel);

        LabelElement<?> shiftTabLabel = new LabelElement<>(
            Spatials.positionXY(-60, 16).width(16).height(16),
            new TextComponent("SHIFT + TAB").withStyle(ChatFormatting.GOLD),
            LabelTextStyle.shadow()
        ).layout(this.translateWorldSpatial());
        this.addElement(shiftTabLabel);

        LabelElement<?> arrows = new LabelElement<>(
            Spatials.positionXY(260, 54).width(16).height(16),
            new TextComponent("← ").withStyle(ChatFormatting.GOLD)
                .append(new TextComponent("scroll").withStyle(ChatFormatting.BLUE))
                .append(new TextComponent(" →").withStyle(ChatFormatting.GOLD)),
            LabelTextStyle.shadow()
        ).layout(this.translateWorldSpatial());
        this.addElement(arrows);

        LabelElement<?> vimArrows = new LabelElement<>(
            Spatials.positionXY(262, 64).width(16).height(16),
            new TextComponent("h ").withStyle(ChatFormatting.GOLD)
                .append(new TextComponent("wheel").withStyle(ChatFormatting.BLUE))
                .append(new TextComponent("  l").withStyle(ChatFormatting.GOLD)),
            LabelTextStyle.shadow()
        ).layout(this.translateWorldSpatial());
        this.addElement(vimArrows);

        LabelElement<?> ctrlLabel = new LabelElement<>(
            Spatials.positionXY(365, 38).width(16).height(16),
            new TextComponent("CTRL").withStyle(ChatFormatting.GOLD),
            LabelTextStyle.shadow()
        ).layout(this.translateWorldSpatial());
        this.addElement(ctrlLabel);

        LabelElement<?> categoryLabelNormal = new LabelElement<>(
            Spatials.positionXY(375, 52).width(16).height(16),
            new TextComponent(ModifierCategory.NORMAL.name()).withStyle(ModifierCategory.NORMAL.getStyle()),
            LabelTextStyle.shadow()
        ).layout(this.translateWorldSpatial());
        this.addElement(categoryLabelNormal);

        LabelElement<?> categoryLabelGreater = new LabelElement<>(
            Spatials.positionXY(375, 62).width(16).height(16),
            new TextComponent(ModifierCategory.GREATER.name()).withStyle(ModifierCategory.GREATER.getStyle()),
            LabelTextStyle.shadow()
        ).layout(this.translateWorldSpatial());
        this.addElement(categoryLabelGreater);

        LabelElement<?> categoryLabelLegendary = new LabelElement<>(
            Spatials.positionXY(375, 72).width(16).height(16),
            new TextComponent(ModifierCategory.LEGENDARY.name()).withStyle(ModifierCategory.LEGENDARY.getStyle()),
            LabelTextStyle.shadow()
        ).layout(this.translateWorldSpatial());
        this.addElement(categoryLabelLegendary);

        LabelElement<?> upLabel = new LabelElement<>(
            Spatials.positionXY(340, 190).width(16).height(16),
            new TextComponent("↑ k").withStyle(ChatFormatting.GOLD),
            LabelTextStyle.shadow()
        ).layout(this.translateWorldSpatial());
        this.addElement(upLabel);
        LabelElement<?> downLabel = new LabelElement<>(
            Spatials.positionXY(340, 204).width(16).height(16),
            new TextComponent("↓ j").withStyle(ChatFormatting.GOLD),
            LabelTextStyle.shadow()
        ).layout(this.translateWorldSpatial());
        this.addElement(downLabel);


        String text = """
            Colored triangles
            represent groups
            of modifiers.
            
            If 2 modifiers are
            in the same group,
            they can't be
            rolled together.
            """;

        String[] array = text.split("\n");
        int labelY = 120;
        for (String s : array) {
            LabelElement<?> textLabel = new LabelElement<>(
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
