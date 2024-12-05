package com.radimous.vhatcaniroll.logic;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public enum ModifierCategory {
    NORMAL,
    GREATER,
    LEGENDARY;

    public String getSymbol() {
        return "✦";
        // greater is not full width :(
//        return switch (this) {
//            case NORMAL -> "";
//            case GREATER -> "⧫";
//            case LEGENDARY -> "✦";
//        };
    }

    public Style getStyle() {
        return switch (this) {
            case NORMAL -> Style.EMPTY.withColor(ChatFormatting.WHITE);
            case GREATER -> Style.EMPTY.withColor(TextColor.fromRgb(5886486));
            case LEGENDARY -> Style.EMPTY.withColor(ChatFormatting.GOLD);
        };
    }

    public int getTierIncrease() {
        return switch (this) {
            case NORMAL -> 0;
            case GREATER -> 1;
            case LEGENDARY -> 2;
        };
    }

    public ModifierCategory next() {
        return values()[(this.ordinal() + 1) % values().length];
    }

    public ModifierCategory previous() {
        return values()[(this.ordinal() + values().length - 1) % values().length];
    }

}
