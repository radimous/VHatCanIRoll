package com.radimous.vhatcaniroll.ui.cards;

import iskallia.vault.client.gui.framework.element.spi.IElement;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;

public interface InnerCardScreen extends IElement {
    float getScroll();
    void setScroll(float scroll);
    InnerCardScreen create(ISpatial spatial);
}
