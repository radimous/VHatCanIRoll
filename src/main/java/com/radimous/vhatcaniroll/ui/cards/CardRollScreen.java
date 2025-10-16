package com.radimous.vhatcaniroll.ui.cards;

import com.radimous.vhatcaniroll.Config;
import com.radimous.vhatcaniroll.ui.GearModifierScreen;
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

public class CardRollScreen extends AbstractElementScreen {
    private final List<IElement> linkElements;
    private InnerCardScreen innerScreen;
    public CardRollScreen() {
        super(new TextComponent("Card Rolls"), ScreenRenderers.getBuffered(), ScreenTooltipRenderer::create);

        linkElements = new ArrayList<>();
        int w = Config.CARD_SCREEN_WIDTH.get();
        this.setGuiSize(Spatials.size(w, 300).height((int) (
            (Minecraft.getInstance().getWindow().getHeight() / Minecraft.getInstance().getWindow().getGuiScale()) *
                Config.CARD_SCREEN_HEIGHT.get())));

        // outer background
        NineSliceElement<?> background = new NineSliceElement<>(
            Spatials.positionXY(0, 30).size(this.getGuiSpatial().width(), this.getGuiSpatial().height() - 30),
            ScreenTextures.DEFAULT_WINDOW_BACKGROUND
        ).layout(this.translateWorldSpatial());

        // window title
        LabelElement<?> windowName = new LabelElement<>(
            Spatials.positionXY(7, 38).size(this.getGuiSpatial().width() / 2 - 7, 20),
            new TextComponent("WIP card rolls").withStyle(ChatFormatting.BLACK),
            LabelTextStyle.defaultStyle()
        ).layout(this.translateWorldSpatial());
//        this.windowNameLabel = windowName;
        this.addElement(background);
        this.addElement(windowName);
        // inner black window

        ISpatial cardRollListSpatial = Spatials.positionXY(7, 50).size(this.getGuiSpatial().width() - 14, this.getGuiSpatial().height() - 57);
        innerScreen = new CardRollListContainer(cardRollListSpatial, "boosterPacks").layout(this.translateWorldSpatial());
        this.addElement(innerScreen);


        createGearButton();

//        createAllButton();
        createBoosterPacksButton();
        createModifiersButton();
        createConditionsButton();
        createScalersButton();
        createTasksButton();
        refreshLinkButtons();
    }

    private void createGearButton() {
        this.addElement(new ButtonElement<>(Spatials.positionXY(this.getGuiSpatial().width() + 20, 3), ScreenTextures.BUTTON_EMPTY_16_TEXTURES, () -> {
            Minecraft.getInstance().setScreen(new GearModifierScreen());
        })).layout((screen, gui, parent, world) -> {
            world.width(21).height(21).translateX(gui.left() - 16).translateY(this.getGuiSpatial().top() + 180);
        }).tooltip(
            Tooltips.single(TooltipDirection.RIGHT, () -> new TextComponent("Gear modifiers"))
        );
        ItemStack chestplateStack = new ItemStack(ModItems.CHESTPLATE);
        this.addElement(
            new FakeItemSlotElement<>(Spatials.positionXY(this.getGuiSpatial().width() + 20, 3), () -> chestplateStack, () -> false, ScreenTextures.EMPTY, ScreenTextures.EMPTY)
                .layout((screen, gui, parent, world) -> world.width(21).height(21).translateX(gui.left() - 16).translateY(this.getGuiSpatial().top() + 180))
        );
    }


// BUTTON_EMPTY_TEXTURES => BUTTON_EMPTY_GRAY_TEXTURES for 19.6
    private void createAllButton() {
        this.addElement(new NineSliceButtonElement<>(Spatials.positionXY(-70, 3), ScreenTextures.BUTTON_EMPTY_TEXTURES, () -> {
            replaceInnerScreen("all");
            })).layout((screen, gui, parent, world) -> {
                world.width(80).height(16).translateX(gui.left() - 16).translateY(this.getGuiSpatial().top() + 27);
            });
        var comp = new TextComponent("All").withStyle(ChatFormatting.BLACK);
        this.addElement(
            new LabelElement<>(Spatials.positionXY(-68, 27), comp, LabelTextStyle.defaultStyle())
                .layout((screen, gui, parent, world) -> world.width(21).height(21).translateX(gui.left() - 16).translateY(this.getGuiSpatial().top() + 27))
        );
    }


    private void createBoosterPacksButton() {
        this.addElement(new NineSliceButtonElement<>(Spatials.positionXY( -70, 23), ScreenTextures.BUTTON_EMPTY_TEXTURES, () -> {
            replaceInnerScreen("boosterPacks");
        })).layout((screen, gui, parent, world) -> {
            world.width(80).height(16).translateX(gui.left() - 16).translateY(this.getGuiSpatial().top() + 27);
        });
        var comp = new TextComponent("Booster Packs").withStyle(ChatFormatting.BLACK);
        this.addElement(
            new LabelElement<>(Spatials.positionXY(-68, 27), comp, LabelTextStyle.defaultStyle())
                .layout((screen, gui, parent, world) -> world.width(21).height(21).translateX(gui.left() - 16).translateY(this.getGuiSpatial().top() + 27))
        );
    }

    private void createModifiersButton() {
        this.addElement(new NineSliceButtonElement<>(Spatials.positionXY(-70, 43), ScreenTextures.BUTTON_EMPTY_TEXTURES, () -> {
            replaceInnerScreen("modifiers");
        })).layout((screen, gui, parent, world) -> {
            world.width(80).height(16).translateX(gui.left() - 16).translateY(this.getGuiSpatial().top() + 27);
        });
        var comp = new TextComponent("Modifiers").withStyle(ChatFormatting.BLACK);
        this.addElement(
            new LabelElement<>(Spatials.positionXY(-68, 47), comp, LabelTextStyle.defaultStyle())
                .layout((screen, gui, parent, world) -> world.width(21).height(21).translateX(gui.left() - 16).translateY(this.getGuiSpatial().top() + 27))
        );
    }

    private void createConditionsButton() {
        this.addElement(new NineSliceButtonElement<>(Spatials.positionXY(-70, 63), ScreenTextures.BUTTON_EMPTY_TEXTURES, () -> {
            replaceInnerScreen("conditions");
        })).layout((screen, gui, parent, world) -> {
            world.width(80).height(16).translateX(gui.left() - 16).translateY(this.getGuiSpatial().top() + 27);
        });
        var comp = new TextComponent("Conditions").withStyle(ChatFormatting.BLACK);
        this.addElement(
            new LabelElement<>(Spatials.positionXY(-68, 67), comp, LabelTextStyle.defaultStyle())
                .layout((screen, gui, parent, world) -> world.width(21).height(21).translateX(gui.left() - 16).translateY(this.getGuiSpatial().top() + 27))
        );
    }

    private void createScalersButton() {
        this.addElement(new NineSliceButtonElement<>(Spatials.positionXY(-70, 83), ScreenTextures.BUTTON_EMPTY_TEXTURES, () -> {
            replaceInnerScreen("scalers");
        })).layout((screen, gui, parent, world) -> {
            world.width(80).height(16).translateX(gui.left() - 16).translateY(this.getGuiSpatial().top() + 27);
        });
        var comp = new TextComponent("Scalers").withStyle(ChatFormatting.BLACK);
        this.addElement(
            new LabelElement<>(Spatials.positionXY(-68, 87), comp, LabelTextStyle.defaultStyle())
                .layout((screen, gui, parent, world) -> world.width(21).height(21).translateX(gui.left() - 16).translateY(this.getGuiSpatial().top() + 27))
        );
    }

    private void createTasksButton() {
        this.addElement(new NineSliceButtonElement<>(Spatials.positionXY(-70, 103), ScreenTextures.BUTTON_EMPTY_TEXTURES, () -> {
            replaceInnerScreen("tasks");
        })).layout((screen, gui, parent, world) -> {
            world.width(80).height(16).translateX(gui.left() - 16).translateY(this.getGuiSpatial().top() + 27);
        });
        var comp = new TextComponent("Tasks").withStyle(ChatFormatting.BLACK);
        this.addElement(
            new LabelElement<>(Spatials.positionXY(-68, 107), comp, LabelTextStyle.defaultStyle())
                .layout((screen, gui, parent, world) -> world.width(21).height(21).translateX(gui.left() - 16).translateY(this.getGuiSpatial().top() + 27))
        );
    }


    private void replaceInnerScreen(String screenType) {
        ISpatial spatial = Spatials.positionXY(this.getGuiSpatial().left() + 7, this.getGuiSpatial().top() + 50).size(this.getGuiSpatial().width() - 14, this.getGuiSpatial().height() - 57);
        this.removeElement(this.innerScreen);
        this.innerScreen = new CardRollListContainer(spatial, screenType);
        this.addElement(innerScreen);
        this.refreshLinkButtons();
        ScreenLayout.requestLayout();
    }

    private void refreshLinkButtons() {
        for (var currEl : linkElements) {
            this.removeElement(currEl);
        }
        linkElements.clear();
        var newLinks = innerScreen.getLinks();
        int x = -70;
        int y = 0;
        for (var link : newLinks.keySet()) {
            if ("start".equals(link) || "end".equals(link)) continue;
            y -= 20;
        }

        for (var link : newLinks.keySet()) {
            if ("start".equals(link) || "end".equals(link)) continue;
            linkElements.add(this.addElement(new NineSliceButtonElement<>(Spatials.positionXY(x, y), ScreenTextures.BUTTON_EMPTY_TEXTURES, () -> {
                this.innerScreen.scrollToLink(link);
            })).layout((screen, gui, parent, world) -> {
                world.width(80).height(16).translateX(gui.left() - 16).translateY(this.getGuiSpatial().bottom());
            }));
            var comp = new TextComponent(link).withStyle(ChatFormatting.BLACK);
            linkElements.add(this.addElement(
                new LabelElement<>(Spatials.positionXY(x + 2, y + 4), comp, LabelTextStyle.defaultStyle())
                    .layout((screen, gui, parent, world) -> world.width(21).height(21).translateX(gui.left() - 16).translateY(this.getGuiSpatial().bottom()))
            ));
            y += 20;
        }
    }

    //TODO: add buttons

}
