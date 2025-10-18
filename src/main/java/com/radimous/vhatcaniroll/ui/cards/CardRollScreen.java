package com.radimous.vhatcaniroll.ui.cards;

import com.radimous.vhatcaniroll.Config;
import com.radimous.vhatcaniroll.logic.CardRolls;
import com.radimous.vhatcaniroll.ui.GearModifierScreen;
import com.radimous.vhatcaniroll.ui.ModifierListContainer;
import iskallia.vault.client.gui.framework.ScreenRenderers;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.*;
import iskallia.vault.client.gui.framework.element.spi.IElement;
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
import java.util.function.Supplier;

public class CardRollScreen extends AbstractElementScreen {
    private final List<IElement> linkElements;
    private InnerCardScreen innerScreen;
    public CardRollScreen() {
        super(new TextComponent("Cards"), ScreenRenderers.getBuffered(), ScreenTooltipRenderer::create);

        linkElements = new ArrayList<>();
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
            new TextComponent("Cards").withStyle(ChatFormatting.BLACK),
            LabelTextStyle.defaultStyle()
        ).layout(this.translateWorldSpatial());
//        this.windowNameLabel = windowName;
        this.addElement(background);
        this.addElement(windowName);
        // inner black window

        ISpatial cardRollListSpatial = Spatials.positionXY(7, 20).size(this.getGuiSpatial().width() - 14, this.getGuiSpatial().height() - 27);
        innerScreen = new BoosterPackListContainer(cardRollListSpatial).layout(this.translateWorldSpatial());
        this.addElement(innerScreen);


        createGearButton();

        createBoosterPacksButton();
        createModifiersButton();
        createConditionsButton();
        createScalersButton();
        createTasksButton();
        refreshLinkButtons();
    }

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
            replaceInnerScreen(() -> new BoosterPackListContainer(Spatials.positionXY(this.getGuiSpatial().left() + 7, this.getGuiSpatial().top() + 20).size(this.getGuiSpatial().width() - 14, this.getGuiSpatial().height() - 27)));
        })).layout((screen, gui, parent, world) -> world.width(80).height(16).translateX(gui.left()).translateY(this.getGuiSpatial().top()));
        var comp = new TextComponent("Booster Packs").withStyle(ChatFormatting.BLACK);
        this.addElement(
            new LabelElement<>(Spatials.positionXY(-80, 7), comp, LabelTextStyle.defaultStyle()).layout(this.translateWorldSpatial())
        );

    }

    private void createModifiersButton() {
        this.addElement(new NineSliceButtonElement<>(Spatials.positionXY(-82, 23), ScreenTextures.BUTTON_EMPTY_GRAY_TEXTURES, () -> {
            replaceInnerScreen(() -> new CardModifierListContainer((Spatials.positionXY(this.getGuiSpatial().left() + 7, this.getGuiSpatial().top() + 20).size(this.getGuiSpatial().width() - 14, this.getGuiSpatial().height() - 27)), null));
        })).layout((screen, gui, parent, world) -> world.width(80).height(16).translateX(gui.left()).translateY(this.getGuiSpatial().top() ));
        var comp = new TextComponent("Modifiers").withStyle(ChatFormatting.BLACK);
        this.addElement(
            new LabelElement<>(Spatials.positionXY(-80, 27), comp, LabelTextStyle.defaultStyle()).layout(this.translateWorldSpatial())
        );
    }

    private void createConditionsButton() {
        this.addElement(new NineSliceButtonElement<>(Spatials.positionXY(-82, 43), ScreenTextures.BUTTON_EMPTY_GRAY_TEXTURES, () -> {
            replaceInnerScreen(() -> new CardConditionsListContainer(Spatials.positionXY(this.getGuiSpatial().left() + 7, this.getGuiSpatial().top() + 20).size(this.getGuiSpatial().width() - 14, this.getGuiSpatial().height() - 27)));
        })).layout((screen, gui, parent, world) -> world.width(80).height(16).translateX(gui.left() ).translateY(this.getGuiSpatial().top() ));
        var comp = new TextComponent("Conditions").withStyle(ChatFormatting.BLACK);
        this.addElement(
            new LabelElement<>(Spatials.positionXY(-80, 47), comp, LabelTextStyle.defaultStyle()).layout(this.translateWorldSpatial())
        );
    }

    private void createScalersButton() {
        this.addElement(new NineSliceButtonElement<>(Spatials.positionXY(-82, 63), ScreenTextures.BUTTON_EMPTY_GRAY_TEXTURES, () -> {
            replaceInnerScreen(() -> new CardScalersListContainer(Spatials.positionXY(this.getGuiSpatial().left() + 7, this.getGuiSpatial().top() + 20).size(this.getGuiSpatial().width() - 14, this.getGuiSpatial().height() - 27)));
        })).layout((screen, gui, parent, world) -> world.width(80).height(16).translateX(gui.left() ).translateY(this.getGuiSpatial().top() ));
        var comp = new TextComponent("Scalers").withStyle(ChatFormatting.BLACK);
        this.addElement(
            new LabelElement<>(Spatials.positionXY(-80, 67), comp, LabelTextStyle.defaultStyle()).layout(this.translateWorldSpatial())
        );
    }

    private void createTasksButton() {
        this.addElement(new NineSliceButtonElement<>(Spatials.positionXY(-82, 83), ScreenTextures.BUTTON_EMPTY_GRAY_TEXTURES, () -> {
            replaceInnerScreen(() -> new CardTasksListContainer(Spatials.positionXY(this.getGuiSpatial().left() + 7, this.getGuiSpatial().top() + 20).size(this.getGuiSpatial().width() - 14, this.getGuiSpatial().height() - 27)));
        })).layout((screen, gui, parent, world) -> world.width(80).height(16).translateX(gui.left() ).translateY(this.getGuiSpatial().top() ));
        var comp = new TextComponent("Tasks").withStyle(ChatFormatting.BLACK);
        this.addElement(
            new LabelElement<>(Spatials.positionXY(-80, 87), comp, LabelTextStyle.defaultStyle()).layout(this.translateWorldSpatial())
        );
    }


    private void replaceInnerScreen(Supplier<InnerCardScreen> screenSupplier) {
        this.removeElement(this.innerScreen);
        this.innerScreen = screenSupplier.get();
        this.addElement(innerScreen);
        this.refreshLinkButtons();
        ScreenLayout.requestLayout();
    }

    private void refreshLinkButtons() {
        for (var currEl : linkElements) {
            this.removeElement(currEl);
        }
        linkElements.clear();
        int x = this.getGuiSpatial().width() + 2;
        int y = 3;
        if (this.innerScreen instanceof CardModifierListContainer) {
            for (String pool : CardRolls.getModifierPools()) {
                if ("start".equals(pool) || "end".equals(pool)) continue;
                linkElements.add(this.addElement(new NineSliceButtonElement<>(Spatials.positionXY(x, y), ScreenTextures.BUTTON_EMPTY_GRAY_TEXTURES, () -> {
                    replaceInnerScreen(() -> new CardModifierListContainer((Spatials.positionXY(this.getGuiSpatial().left() + 7, this.getGuiSpatial().top() + 20).size(this.getGuiSpatial().width() - 14, this.getGuiSpatial().height() - 27)), pool));
                })).layout((screen, gui, parent, world) -> {
                    world.width(80).height(16).translateX(gui.left()).translateY(this.getGuiSpatial().top());
                }));
                var comp = new TextComponent(pool).withStyle(ChatFormatting.BLACK);
                linkElements.add(this.addElement(
                    new LabelElement<>(Spatials.positionXY(x + 2, y + 4), comp, LabelTextStyle.defaultStyle())
                        .layout((screen, gui, parent, world) -> world.width(21).height(21).translateX(gui.left()).translateY(this.getGuiSpatial().top()))
                ));
                y += 20;
            }
        }
    }

}
