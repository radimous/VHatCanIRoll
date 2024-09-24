package com.radimous.vhatcaniroll;

import com.mojang.blaze3d.platform.InputConstants;
import iskallia.vault.client.gui.framework.ScreenRenderers;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.FakeItemSlotElement;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.NineSliceButtonElement;
import iskallia.vault.client.gui.framework.element.NineSliceElement;
import iskallia.vault.client.gui.framework.element.TabElement;
import iskallia.vault.client.gui.framework.element.TextInputElement;
import iskallia.vault.client.gui.framework.element.TextureAtlasElement;
import iskallia.vault.client.gui.framework.element.spi.ILayoutStrategy;
import iskallia.vault.client.gui.framework.render.ScreenTooltipRenderer;
import iskallia.vault.client.gui.framework.screen.AbstractElementScreen;
import iskallia.vault.client.gui.framework.screen.layout.ScreenLayout;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.IPosition;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import iskallia.vault.client.gui.overlay.VaultBarOverlay;
import iskallia.vault.init.ModConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GearModifierScreen extends AbstractElementScreen {
    //TODO: code cleanup - variable naming, magic numbers, some logic reordering etc

    private ModifierListContainer modifierList;
    private final TextInputElement<?> lvlInput;
    private boolean legendary;
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
        createLvlButtons();
        createLegendaryButton();

        // inner black window
        this.modifierList = new ModifierListContainer(
            Spatials.positionXY(7, 50).size(this.getGuiSpatial().width() - 14, this.getGuiSpatial().height() - 57),
            this)
            .layout(this.translateWorldSpatial());
        this.addElement(this.modifierList);
    }

    public ItemStack getCurrGear() {
        return VHatCanIRoll.getVaultGearItems().get(currIndex);
    }

    public int getCurrLvl() {
        try {
            return Integer.parseInt(this.lvlInput.getInput());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void increaseLvl() {
        this.lvlInput.setInput(String.valueOf(this.getCurrLvl() + 1));
    }

    private void decreaseLvl() {
        this.lvlInput.setInput(String.valueOf(this.getCurrLvl() - 1));
    }

    private void updateModifierList(boolean keepScroll) {
        var oldScroll = this.modifierList.getScroll();
        this.removeElement(this.modifierList);
        this.modifierList = new ModifierListContainer(
            Spatials.positionXY(7, 50).size(this.getGuiSpatial().width() - 14, this.getGuiSpatial().height() - 57),
            this)
            .layout(this.translateWorldSpatial());
        if (keepScroll) {
            this.modifierList.setScroll(oldScroll);
        }
        this.addElement(this.modifierList);
        ScreenLayout.requestLayout();

    }

    private ILayoutStrategy translateWorldSpatial() {
        return (screen, gui, parent, world) -> world.translateXY(this.getGuiSpatial());
    }

    private IPosition getPos(int tabIndex, boolean selected) {
        if (tabIndex < 11){
            return Spatials.positionXY(5 + tabIndex * 30, 2 + (selected ? 0 : 4));
        }
        return Spatials.positionXY(337 + (selected? 0:3), 35 + (tabIndex-11) * 30);
    }
    private ISpatial getItemPos(int tabIndex) {
        if (tabIndex < 11){
            return Spatials.positionXY(10 + tabIndex * 30, 11);
        }
        return Spatials.positionXY(342, 40 + (tabIndex-11) * 30);

    }
    // actual tab
    private TabElement<?> getTabElement(int tabIndex, boolean selected) {
        if (tabIndex < 11){
            return new TabElement<>(getPos(tabIndex, selected),
                new TextureAtlasElement<>(
                    selected ? ScreenTextures.TAB_BACKGROUND_TOP_SELECTED : ScreenTextures.TAB_BACKGROUND_TOP),
                new TextureAtlasElement<>(ScreenTextures.EMPTY), () -> switchTab(tabIndex))
                .layout(this.translateWorldSpatial());
        }
        return new TabElement<>(getPos(tabIndex, selected),
            new TextureAtlasElement<>(
                selected ? ScreenTextures.TAB_BACKGROUND_RIGHT_SELECTED : ScreenTextures.TAB_BACKGROUND_RIGHT),
            new TextureAtlasElement<>(ScreenTextures.EMPTY), () -> switchTab(tabIndex))
            .layout(this.translateWorldSpatial());
    }

    private void switchTab(int tabIndex) {
        for (int i = 0; i < VHatCanIRoll.getVaultGearItems().size(); i++) {
            this.removeElement(tabs.get(i));
            TabElement<?> tab = getTabElement(i, i == tabIndex);
            tabs.set(i, tab);
            this.addElement(tab);
        }
        this.currIndex = tabIndex;
        updateModifierList(false);
    }

    private TextInputElement<?> createLvlInput() {
        ScrollableTextInputElement inputElement = this.addElement(
            new ScrollableTextInputElement(Spatials.positionXY(this.getGuiSpatial().width() - 54 - 13, 36).size(26, 12),
                Minecraft.getInstance().font)
                .layout((screen, gui, parent, world) -> world.translateXY(gui))
        );
        inputElement.adjustEditBox(editBox -> {
            editBox.setFilter(this::isValidLevel);
            editBox.setMaxLength(3);
            editBox.setValue(String.valueOf(VaultBarOverlay.vaultLevel));
        });
        inputElement.onTextChanged(s -> updateModifierList(true));
        return inputElement;
    }

    private boolean isValidLevel(String input){
        // reminds me of original vault filters
        if (input.isEmpty()) {
            return true;
        }
        int lvl;
        try {
            lvl = Integer.parseInt(input);
        } catch (NumberFormatException numberformatexception) {
            return false;
        }
        if (lvl <= ModConfigs.LEVELS_META.getMaxLevel() && lvl >= 0) {
            ScreenLayout.requestLayout();
            return true;
        }
        if (lvl <= Config.MAX_LEVEL_OVERRIDE.get() && lvl >= 0) {
            ScreenLayout.requestLayout();
            return true;
        }
        return false;
    }

    private void createLvlButtons() {
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
                ScreenTextures.BUTTON_EMPTY_TEXTURES, this::decreaseLvl).layout(this.translateWorldSpatial());
        NineSliceButtonElement<?> btnPlus =
            new NineSliceButtonElement<>(Spatials.positionXY(this.getGuiSpatial().width() - 25 - 13, 35).size(15, 14),
                ScreenTextures.BUTTON_EMPTY_TEXTURES, this::increaseLvl).layout(this.translateWorldSpatial());
        this.addElement(btnMinus);
        this.addElement(minusLabel);
        this.addElement(plusLabel);
        this.addElement(btnPlus);
    }

    private void toggleLegend() {
        this.legendary = !this.legendary;
        updateLegendaryLabel();
        updateModifierList(true);
    }

    private void updateLegendaryLabel() {
        if (this.legendaryLabel != null) {
            this.removeElement(this.legendaryLabel);
        }
        var formatting = this.legendary ? ChatFormatting.GOLD : ChatFormatting.WHITE;
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

    private void createTabs() {
        for (int i = 0; i < VHatCanIRoll.getVaultGearItems().size(); i++) {
            addTab(i);
            addFakeItemSlot(i);
        }
    }

    private void addTab(int index) {
        TabElement<?> tab = getTabElement(index, index == currIndex);
        tabs.add(this.addElement(tab));
    }

    private void addFakeItemSlot(int index) {
        ItemStack gearItem = VHatCanIRoll.getVaultGearItems().get(index);
        this.addElement(
            new FakeItemSlotElement<>(getItemPos(index), () -> gearItem, () -> false,
                ScreenTextures.EMPTY, ScreenTextures.EMPTY).layout(
                this.translateWorldSpatial()));
    }

    public boolean isLegendary() {
        return legendary;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // left/right to increase/decrease lvl
        if (keyCode == InputConstants.KEY_L || keyCode == InputConstants.KEY_RIGHT) {
            increaseLvl();
        }
        if (keyCode == InputConstants.KEY_H || keyCode == InputConstants.KEY_LEFT) {
            decreaseLvl();
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
            switchTab((currIndex + 1) % VHatCanIRoll.getVaultGearItems().size());
        }
        // shift+tab to previous gear item
        if (keyCode == InputConstants.KEY_TAB && hasShiftDown()) {
            switchTab((currIndex - 1 + VHatCanIRoll.getVaultGearItems().size()) % VHatCanIRoll.getVaultGearItems().size());
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