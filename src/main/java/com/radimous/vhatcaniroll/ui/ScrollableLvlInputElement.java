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
            editBox.setValue(String.valueOf(VaultBarOverlay.vaultLevel));
        });
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


    public int getValue() {
        return parseInt(this.getInput());
    }

    public void setValue(int value) {
        if (isValidLevel(value)) {
            this.setInput(String.valueOf(value));
        }
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

    public boolean isValidLevel(int lvl){
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

}
