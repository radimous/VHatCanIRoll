package com.radimous.vhatcaniroll.ui.greedquests.inner;

import iskallia.vault.client.gui.framework.element.spi.IElement;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;

public interface InnerGreedScreen extends IElement {
    float getScroll();
    void setScroll(float scroll);
    InnerGreedScreen create(ISpatial spatial);
}
