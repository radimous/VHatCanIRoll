package com.radimous.vhatcaniroll.ui.greedquests;

import com.radimous.vhatcaniroll.Config;
import com.radimous.vhatcaniroll.ui.gear.GearModifierScreen;
import com.radimous.vhatcaniroll.ui.greedquests.inner.GreedQuestListContainer;
import com.radimous.vhatcaniroll.ui.greedquests.inner.InnerGreedScreen;
import iskallia.vault.client.gui.framework.ScreenRenderers;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.*;
import iskallia.vault.client.gui.framework.element.spi.IElement;
import iskallia.vault.client.gui.framework.element.spi.ILayoutElement;
import iskallia.vault.client.gui.framework.render.ScreenTooltipRenderer;
import iskallia.vault.client.gui.framework.render.TooltipDirection;
import iskallia.vault.client.gui.framework.render.Tooltips;
import iskallia.vault.client.gui.framework.screen.AbstractElementScreen;
import iskallia.vault.client.gui.framework.screen.layout.ScreenLayout;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import iskallia.vault.init.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class GreedQuestScreen extends AbstractElementScreen {
    private final List<IElement> linkElements = new ArrayList<>();
    private InnerGreedScreen innerScreen;
    public GreedQuestScreen() {
        super(new TextComponent("Greed"), ScreenRenderers.getBuffered(), ScreenTooltipRenderer::create);
    }

    @Override protected void init() {
        float scroll = this.innerScreen != null ? this.innerScreen.getScroll() : 0;
        linkElements.clear();
        this.removeAllElements();
        int w = Config.CARD_SCREEN_WIDTH.get();
        if ((Minecraft.getInstance().getWindow().getWidth() / Minecraft.getInstance().getWindow().getGuiScale()) - 60 < w) {
            w = (int) ((Minecraft.getInstance().getWindow().getWidth() / Minecraft.getInstance().getWindow().getGuiScale()) - 170);
        }
        this.setGuiSize(Spatials.size(w, 300).height((int) (
            (Minecraft.getInstance().getWindow().getHeight() / Minecraft.getInstance().getWindow().getGuiScale()) *
                Config.CARD_SCREEN_HEIGHT.get())));

        // outer background
        NineSliceElement<?> background = new NineSliceElement<>(
            Spatials.positionXY(0, 0).size(this.getGuiSpatial().width(), this.getGuiSpatial().height()),
            ScreenTextures.DEFAULT_WINDOW_BACKGROUND
        ).layout(this.translateWorldSpatial());

        // window title
        LabelElement<?> windowName = new LabelElement<>(
            Spatials.positionXY(7, 8).size(this.getGuiSpatial().width() / 2 - 7, 20),
            new TextComponent("Greed").withStyle(ChatFormatting.BLACK),
            LabelTextStyle.defaultStyle()
        ).layout(this.translateWorldSpatial());
        this.addElement(background);
        this.addElement(windowName);
        // inner black window

        ISpatial greedQuestListSpatial = Spatials.positionXY(7, 20).size(this.getGuiSpatial().width() - 14, this.getGuiSpatial().height() - 27);
        if (this.innerScreen == null) {
            this.innerScreen = this.addElement(new GreedQuestListContainer(greedQuestListSpatial).layout(this.translateWorldSpatial()));
        } else {
            this.innerScreen = this.addElement(this.innerScreen.create(greedQuestListSpatial));
            if (innerScreen instanceof ILayoutElement<?> layoutElement) {
                layoutElement.layout(this.translateWorldSpatial());
            }
        }
        this.innerScreen.setScroll(scroll);

        createGearButton();

//        createBoosterPacksButton();
        super.init();
    }

    //<editor-fold desc="Screen Switching Buttons">
    private void createGearButton() {
        this.addElement(new ButtonElement<>(Spatials.positionXY(-20, 133), ScreenTextures.BUTTON_EMPTY_16_TEXTURES, () -> {
            Minecraft.getInstance().setScreen(new GearModifierScreen());
        })).layout((screen, gui, parent, world) -> world.width(21).height(21).translateX(gui.left()).translateY(this.getGuiSpatial().top())).tooltip(
            Tooltips.single(TooltipDirection.RIGHT, () -> new TextComponent("Gear modifiers"))
        );
        ItemStack chestplateStack = new ItemStack(ModItems.CHESTPLATE);
        this.addElement(
            new FakeItemSlotElement<>(Spatials.positionXY(-20, 133), () -> chestplateStack, () -> false, ScreenTextures.EMPTY, ScreenTextures.EMPTY)
                .layout((screen, gui, parent, world) -> world.width(21).height(21).translateX(gui.left()).translateY(this.getGuiSpatial().top()))
        );
    }

    private void createBoosterPacksButton() {
        this.addElement(new NineSliceButtonElement<>(Spatials.positionXY( -82, 3), ScreenTextures.BUTTON_EMPTY_GRAY_TEXTURES, () -> {
            replaceInnerScreen((spatial) -> new GreedQuestListContainer(Spatials.positionXY(7, 20).size(spatial.width() - 14, spatial.height() - 27)).layout(translateWorldSpatial()));
        })).layout((screen, gui, parent, world) -> world.width(80).height(16).translateX(gui.left()).translateY(this.getGuiSpatial().top()));
        var comp = new TextComponent("Quests").withStyle(ChatFormatting.BLACK);
        this.addElement(
            new LabelElement<>(Spatials.positionXY(-80, 7), comp, LabelTextStyle.defaultStyle()).layout(this.translateWorldSpatial())
        );

    }

    //</editor-fold>

    private void replaceInnerScreen(Function<ISpatial, InnerGreedScreen> screenSupplier) {
        this.removeElement(this.innerScreen);
        this.innerScreen = screenSupplier.apply(this.getGuiSpatial());
        this.addElement(innerScreen);
        ScreenLayout.requestLayout();
    }

}
