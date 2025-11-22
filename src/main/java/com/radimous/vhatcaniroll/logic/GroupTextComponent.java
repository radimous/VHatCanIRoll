package com.radimous.vhatcaniroll.logic;

import net.minecraft.network.chat.TextComponent;

public class GroupTextComponent extends TextComponent {
    private TextComponent original;
    private TextComponent groupTooltip;
    private GroupTextComponent(String p_131286_) {
        super(p_131286_);
    }

    public GroupTextComponent(TextComponent tc, TextComponent groupTooltip) {
        super(tc.getText());
        original = tc;
        this.groupTooltip = groupTooltip;
    }

    public TextComponent getTextComponent() {
        return original;
    }

    public TextComponent getGroupTooltip() {
        return groupTooltip;
    }
}
