package com.radimous.vhatcaniroll;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue VAULT_SCREEN_BUTTON;
    public static final ForgeConfigSpec.BooleanValue ALLOW_DUPE;
    public static final ForgeConfigSpec.IntValue BUTTON_X;
    public static final ForgeConfigSpec.IntValue BUTTON_Y;
    public static final ForgeConfigSpec.BooleanValue COMBINE_LVL_TO_ABILITIES;
    public static final ForgeConfigSpec.IntValue MAX_LEVEL_OVERRIDE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        COMBINE_LVL_TO_ABILITIES = builder
            .comment("combine +lvl to abilities into one row")
            .define("combineAddedLvlToAbilities", true);

        builder.push("BUTTON");
        VAULT_SCREEN_BUTTON = builder
            .comment("open VHat can I roll? from vault screen")
            .define("vaultScreenButton", true);
        BUTTON_X = builder
            .comment("x position of the button")
            .defineInRange("buttonPositionX", 5, Integer.MIN_VALUE, Integer.MAX_VALUE);
        BUTTON_Y = builder
            .comment("y position of the button")
            .defineInRange("buttonPositionY", 109, Integer.MIN_VALUE, Integer.MAX_VALUE);
        builder.pop();

        builder.push("DEBUG");
        ALLOW_DUPE = builder
            .comment("allow duplicate modifiers")
            .define("allowDupe", false);
        MAX_LEVEL_OVERRIDE = builder
            .comment("override max level")
            .defineInRange("maxLevelOverride", -1, -1, Integer.MAX_VALUE);
        builder.pop();

        SPEC = builder.build();
    }
}
