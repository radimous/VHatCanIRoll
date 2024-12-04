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
    public static final ForgeConfigSpec.BooleanValue SHOW_ABILITY_ENHANCEMENTS;    
    public static final ForgeConfigSpec.BooleanValue SHOW_WEIGHT;
    public static final ForgeConfigSpec.BooleanValue SHOW_CHANCE;


    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        COMBINE_LVL_TO_ABILITIES = builder
            .comment("combine +lvl to abilities into one row")
            .define("combineAddedLvlToAbilities", false);

        builder.push("BUTTON");
        VAULT_SCREEN_BUTTON = builder
            .comment("open VHat can I roll? from vault screen")
            .define("vaultScreenButton", true);
        BUTTON_X = builder
            .comment("x position of the button")
            .defineInRange("buttonPositionX", 5, Integer.MIN_VALUE, Integer.MAX_VALUE);
        
        //TODO: auto move the button to 130 if QOL Hunters is loaded (button conflict)
        BUTTON_Y = builder
            .comment("y position of the button")
            .defineInRange("buttonPositionY", 109, Integer.MIN_VALUE, Integer.MAX_VALUE);
        builder.pop();

        builder.push("DEBUG");
        ALLOW_DUPE = builder
            .comment("allow duplicate modifiers")
            .define("allowDupe", false);
        builder.pop();

        MAX_LEVEL_OVERRIDE = builder
            .comment("override max level")
            .defineInRange("maxLevelOverride", -1, -1, Integer.MAX_VALUE);

        SHOW_ABILITY_ENHANCEMENTS = builder
            .comment("show ability enhancements")
            .define("showAbilityEnhancements", false);

        SHOW_WEIGHT = builder
            .comment("show weight")
            .define("showWeight", false);

        SHOW_CHANCE = builder
            .comment("show chance")
            .define("showChance", true);
        SPEC = builder.build();
    }
}
