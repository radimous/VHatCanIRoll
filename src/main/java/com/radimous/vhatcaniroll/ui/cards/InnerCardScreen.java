package com.radimous.vhatcaniroll.ui.cards;

import com.radimous.vhatcaniroll.logic.ModifierCategory;
import iskallia.vault.client.gui.framework.element.spi.IElement;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public interface InnerCardScreen extends IElement {
    float getScroll();
    void setScroll(float scroll);
    Map<String, Integer> getLinks();
    void scrollToLink(String target);
    InnerCardScreen create(ISpatial spatial, String screenType);
}
