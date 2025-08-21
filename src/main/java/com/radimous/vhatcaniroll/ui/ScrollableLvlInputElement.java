package com.radimous.vhatcaniroll.ui;

import com.radimous.vhatcaniroll.Config;

import iskallia.vault.client.gui.framework.element.TextInputElement;
import iskallia.vault.client.gui.framework.screen.layout.ScreenLayout;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.overlay.VaultBarOverlay;
import iskallia.vault.init.ModConfigs;
import net.minecraft.client.gui.Font;

public class ScrollableLvlInputElement extends TextInputElement<ScrollableLvlInputElement> {
    public ScrollableLvlInputElement(ISpatial spatial, Font font) {
        super(spatial, font);
        this.adjustEditBox(editBox -> {
            editBox.setFilter(s -> isValidLevel(parseInt(s)));
            editBox.setMaxLength(3);
        });
        this.setValue(VaultBarOverlay.vaultLevel);
    }

    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.isMouseOver(mouseX, mouseY)) {
            int val = parseInt(this.getInput());
            val += delta > 0 ? 1 : -1;
            this.setInput(String.valueOf(val));
            return true;
        }
        return super.onMouseScrolled(mouseX, mouseY, delta);
    }

    @Override public boolean onMouseClicked(double mouseX, double mouseY, int buttonIndex) {
        if (buttonIndex == 1) { // right
            this.setInput("");
        }
        if (buttonIndex == 2){ // middle
            this.setValue(VaultBarOverlay.vaultLevel);
        }
        return super.onMouseClicked(mouseX, mouseY, buttonIndex);
    }

    @Override public boolean charTyped(char charTyped, int keyCode) {
        if (!Character.isDigit(charTyped)) {
            return false;
        }
        return super.charTyped(charTyped, keyCode);
    }

    public int getValue() {
        return parseInt(this.getInput());
    }

    public void setValue(int value) {
        this.setInput(String.valueOf(getClampedLvl(value)));
    }

    public void increment() {
        this.setValue(getValue() + 1);
    }

    public void decrement() {
        this.setValue(getValue() - 1);
    }

    private int parseInt(String val) {
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getClampedLvl(int lvl) {
        if (lvl < 0) {
            return 0;
        }
        return Math.min(lvl, getMaxLevel());
    }

    public boolean isValidLevel(int lvl){
      if (lvl <= getMaxLevel() && lvl >= 0) {
            ScreenLayout.requestLayout();
            return true;
        }

        return false;
    }

    public static int getMaxLevel() {
        int maxGameLvl = ModConfigs.LEVELS_META.getMaxLevel();
        int maxOverride =  Config.MAX_LEVEL_OVERRIDE.get();
        return Math.max(maxGameLvl, maxOverride);
    }

}
