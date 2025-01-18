package com.radimous.vhatcaniroll.ui;

import com.radimous.vhatcaniroll.logic.ModifierCategory;
import iskallia.vault.client.gui.framework.element.spi.IElement;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import net.minecraft.world.item.ItemStack;

public interface InnerGearScreen extends IElement {
    float getScroll();
    void setScroll(float scroll);
    InnerGearScreen create(ISpatial spatial, int lvl, ModifierCategory modifierCategory, ItemStack gearPiece);
}
