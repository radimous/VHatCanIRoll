package com.radimous.vhatcaniroll.ui;

import com.mojang.blaze3d.platform.InputConstants;
import com.radimous.vhatcaniroll.Config;
import com.radimous.vhatcaniroll.logic.Items;

import iskallia.vault.client.gui.framework.ScreenRenderers;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.FakeItemSlotElement;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.NineSliceButtonElement;
import iskallia.vault.client.gui.framework.element.NineSliceElement;
import iskallia.vault.client.gui.framework.element.TabElement;
import iskallia.vault.client.gui.framework.element.TextureAtlasElement;
import iskallia.vault.client.gui.framework.element.spi.ILayoutStrategy;
import iskallia.vault.client.gui.framework.render.ScreenTooltipRenderer;
import iskallia.vault.client.gui.framework.screen.AbstractElementScreen;
import iskallia.vault.client.gui.framework.screen.layout.ScreenLayout;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.IPosition;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GearModifierScreen extends AbstractElementScreen {
    //TODO: remove magic numbers

    private ModifierListContainer modifierList;
    private final ScrollableLvlInputElement lvlInput;
    private int tierIncrease = 0;
    private LabelElement<?> legendaryLabel;

    private int currIndex = 0;
    private final List<TabElement<?>> tabs = new ArrayList<>();

    public GearModifierScreen() {
        super(new TranslatableComponent("vhatcaniroll.screen.title"), ScreenRenderers.getBuffered(), ScreenTooltipRenderer::create);

        // make screen size 95% of the window height and width that looks good
        this.setGuiSize(Spatials.size(340, 300).height((int) (
            (Minecraft.getInstance().getWindow().getHeight() / Minecraft.getInstance().getWindow().getGuiScale()) *
                0.95)));

        // outer background
        NineSliceElement<?> background = new NineSliceElement<>(
            Spatials.positionXY(0, 30).size(this.getGuiSpatial().width(), this.getGuiSpatial().height() - 30),
            ScreenTextures.DEFAULT_WINDOW_BACKGROUND
        ).layout(this.translateWorldSpatial());

        // window title
        LabelElement<?> windowName = new LabelElement<>(
            Spatials.positionXY(7, 38).size(this.getGuiSpatial().width() / 2 - 7, 20),
            this.title.copy().withStyle(ChatFormatting.BLACK),
            LabelTextStyle.defaultStyle()
        ).layout(this.translateWorldSpatial());


        this.addElement(background);
        this.addElement(windowName);

        createTabs();

        this.lvlInput = this.addElement(createLvlInput());
        
        createLvlButtons(lvlInput);
        createLegendaryButton();

        // inner black window
        ISpatial modListSpatial = Spatials.positionXY(7, 50).size(this.getGuiSpatial().width() - 14, this.getGuiSpatial().height() - 57);
        this.modifierList = new ModifierListContainer(modListSpatial, lvlInput.getValue(), tierIncrease, getCurrGear()).layout(this.translateWorldSpatial());
        this.addElement(this.modifierList);
    }

    // helper methods

    public ItemStack getCurrGear() {
        return Items.getVaultGearItems().get(currIndex);
    }

    /**
     * Update the modifier list with the current gear item and lvl and legendary flag
     * @param keepScroll whether to keep the current scroll position
     */
    private void updateModifierList(boolean keepScroll) {
        var oldScroll = this.modifierList.getScroll();

        this.removeElement(this.modifierList);
        ISpatial modListSpatial = Spatials.positionXY(7, 50).size(this.getGuiSpatial().width() - 14, this.getGuiSpatial().height() - 57);
        this.modifierList = new ModifierListContainer(modListSpatial, lvlInput.getValue(), tierIncrease, getCurrGear()).layout(this.translateWorldSpatial());
        
        if (keepScroll) {
            this.modifierList.setScroll(oldScroll);
        }
        
        this.addElement(this.modifierList);
        ScreenLayout.requestLayout();

    }

    // pulled from QuestOverviewElementScreen
    private ILayoutStrategy translateWorldSpatial() {
        return (screen, gui, parent, world) -> world.translateXY(this.getGuiSpatial());
    }


    // tabs

    /**
     * Get the position where tab should be drawn
     * @param tabIndex
     * @param selected
     * @return
     */
    private IPosition getTabPos(int tabIndex, boolean selected) {
        if (tabIndex < 11){ // top tabs
            return Spatials.positionXY(5 + tabIndex * 30, 2 + (selected ? 0 : 4));
        }
        // right tabs (only needed for wold's rn)
        return Spatials.positionXY(337 + (selected ? 0:3), 35 + (tabIndex-11) * 30);
    }

    /**
     * Get the position where item "icon" should be drawn
     * @param tabIndex
     * @return
     */
    private ISpatial getItemPos(int tabIndex) {
        if (tabIndex < 11){
            return Spatials.positionXY(10 + tabIndex * 30, 11);
        }
        return Spatials.positionXY(342, 40 + (tabIndex-11) * 30);

    }

    private TabElement<?> getTabElement(int tabIndex, boolean selected) {
        if (tabIndex < 11){ // top tabs
            return new TabElement<>(getTabPos(tabIndex, selected),
                new TextureAtlasElement<>(
                    selected ? ScreenTextures.TAB_BACKGROUND_TOP_SELECTED : ScreenTextures.TAB_BACKGROUND_TOP),
                new TextureAtlasElement<>(ScreenTextures.EMPTY), () -> switchTab(tabIndex))
                .layout(this.translateWorldSpatial());
        }

        return new TabElement<>(getTabPos(tabIndex, selected), // right tabs (only needed for wold's rn)
            new TextureAtlasElement<>(
                selected ? ScreenTextures.TAB_BACKGROUND_RIGHT_SELECTED : ScreenTextures.TAB_BACKGROUND_RIGHT),
            new TextureAtlasElement<>(ScreenTextures.EMPTY), () -> switchTab(tabIndex))
            .layout(this.translateWorldSpatial());
    }

    private void switchTab(int tabIndex) {
        for (int i = 0; i < Items.getVaultGearItems().size(); i++) {
            this.removeElement(tabs.get(i));
            TabElement<?> tab = getTabElement(i, i == tabIndex);
            tabs.set(i, tab);
            this.addElement(tab);
        }
        this.currIndex = tabIndex;
        updateModifierList(false);
    }

    private void createTabs() {
        for (int i = 0; i < Items.getVaultGearItems().size(); i++) {
            addTab(i);
            addFakeItemSlot(i);
        }
    }

    private void addTab(int index) {
        TabElement<?> tab = getTabElement(index, index == currIndex);
        tabs.add(this.addElement(tab));
    }

    private void addFakeItemSlot(int index) {
        ItemStack gearItem = Items.getVaultGearItems().get(index);
        this.addElement(
            new FakeItemSlotElement<>(getItemPos(index), () -> gearItem, () -> false,
                ScreenTextures.EMPTY, ScreenTextures.EMPTY).layout(
                this.translateWorldSpatial()));
    }


    // header

    private ScrollableLvlInputElement createLvlInput() {
        ScrollableLvlInputElement inputElement = this.addElement(
            new ScrollableLvlInputElement(Spatials.positionXY(this.getGuiSpatial().width() - 54 - 13, 36).size(26, 12),
                Minecraft.getInstance().font)
                .layout(this.translateWorldSpatial())
        );
        
        inputElement.onTextChanged(s -> updateModifierList(true));
        return inputElement;
    }
    
    private void createLvlButtons(ScrollableLvlInputElement lvlInput) {
        LabelElement<?> minusLabel =
            new LabelElement<>(Spatials.positionXY(this.getGuiSpatial().width() - 68 - 13, 38), new TextComponent("-"),
                LabelTextStyle.border4(ChatFormatting.BLACK))
                .layout(this.translateWorldSpatial());
        LabelElement<?> plusLabel =
            new LabelElement<>(Spatials.positionXY(this.getGuiSpatial().width() - 21 - 13, 38), new TextComponent("+"),
                LabelTextStyle.border4(ChatFormatting.BLACK))
                .layout(this.translateWorldSpatial());
        NineSliceButtonElement<?> btnMinus =
            new NineSliceButtonElement<>(Spatials.positionXY(this.getGuiSpatial().width() - 72 - 13, 35).size(15, 14),
                ScreenTextures.BUTTON_EMPTY_TEXTURES, lvlInput::increment).layout(this.translateWorldSpatial());
        NineSliceButtonElement<?> btnPlus =
            new NineSliceButtonElement<>(Spatials.positionXY(this.getGuiSpatial().width() - 25 - 13, 35).size(15, 14),
                ScreenTextures.BUTTON_EMPTY_TEXTURES, lvlInput::decrement).layout(this.translateWorldSpatial());
        this.addElement(btnMinus);
        this.addElement(minusLabel);
        this.addElement(plusLabel);
        this.addElement(btnPlus);
    }

    private void toggleLegend() {
        this.tierIncrease = (tierIncrease + 1) % 3;
        updateLegendaryLabel();
        updateModifierList(true);
    }

    private void updateLegendaryLabel() {
        if (this.legendaryLabel != null) {
            this.removeElement(this.legendaryLabel);
        }
        var formatting = tierIncrease == 2 ? ChatFormatting.GOLD : tierIncrease == 1 ? ChatFormatting.AQUA : ChatFormatting.WHITE;
        this.legendaryLabel = new LabelElement<>(Spatials.positionXY(this.getGuiSpatial().width() - 5 - 13, 38),
            new TextComponent("✦").withStyle(formatting), LabelTextStyle.defaultStyle())
            .layout(this.translateWorldSpatial());
        this.addElement(legendaryLabel);
    }

    private void createLegendaryButton() {
        updateLegendaryLabel();
        NineSliceButtonElement<?> btnLegend =
            new NineSliceButtonElement<>(Spatials.positionXY(this.getGuiSpatial().width() - 8 - 13, 35).size(14, 14),
                ScreenTextures.BUTTON_EMPTY_TEXTURES, this::toggleLegend).layout(this.translateWorldSpatial());
        this.addElement(btnLegend);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // left/right to increase/decrease lvl
        if (keyCode == InputConstants.KEY_L || keyCode == InputConstants.KEY_RIGHT) {
            lvlInput.increment();
        }
        if (keyCode == InputConstants.KEY_H || keyCode == InputConstants.KEY_LEFT) {
            lvlInput.decrement();
        }
        // up/down to scroll up/down
        if (keyCode == InputConstants.KEY_K || keyCode == InputConstants.KEY_UP) {
            this.modifierList.onMouseScrolled(0, 0, 1);
        }
        if (keyCode == InputConstants.KEY_J || keyCode == InputConstants.KEY_DOWN) {
            this.modifierList.onMouseScrolled(0, 0, -1);
        }
        // tab to next gear item
        if (keyCode == InputConstants.KEY_TAB && !hasShiftDown()) {
            switchTab((currIndex + 1) % Items.getVaultGearItems().size());
        }
        // shift+tab to previous gear item
        if (keyCode == InputConstants.KEY_TAB && hasShiftDown()) {
            switchTab((currIndex - 1 + Items.getVaultGearItems().size()) % Items.getVaultGearItems().size());
        }
        // alt to toggle legendary
        if (keyCode == InputConstants.KEY_LALT || keyCode == InputConstants.KEY_RALT) {
            toggleLegend();
        }
        // ctrl + , to toggle compact +lvl to abilities
        if (keyCode == InputConstants.KEY_COMMA && hasControlDown()) {
            Config.COMBINE_LVL_TO_ABILITIES.set(!Config.COMBINE_LVL_TO_ABILITIES.get());
            updateModifierList(true);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}