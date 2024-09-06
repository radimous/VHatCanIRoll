package com.radimous.vhatcaniroll;

import iskallia.vault.client.gui.framework.element.TextInputElement;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import net.minecraft.client.gui.Font;

public class ScrollableTextInputElement extends TextInputElement<ScrollableTextInputElement> {
    public ScrollableTextInputElement(ISpatial spatial, Font font) {
        super(spatial, font);
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

    private int parseInt(String val) {
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
