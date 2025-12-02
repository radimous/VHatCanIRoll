package com.radimous.vhatcaniroll.logic;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.List;

public class GroupTextComponent extends TextComponent {
    private TextComponent original;
    private List<Component>  groupTooltip;
    private GroupTextComponent(String p_131286_) {
        super(p_131286_);
    }

    public GroupTextComponent(TextComponent tc, List<Component> groupTooltip) {
        super(tc.getText());
        original = tc;
        this.groupTooltip = groupTooltip;
    }

    public TextComponent getTextComponent() {
        return original;
    }

    public List<Component> getGroupTooltip() {
        return groupTooltip;
    }
}
