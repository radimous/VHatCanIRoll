package com.radimous.vhatcaniroll;

import iskallia.vault.config.gear.VaultGearTierConfig;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class Config {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue VAULT_SCREEN_BUTTON;
    public static final ForgeConfigSpec.IntValue BUTTON_X;
    public static final ForgeConfigSpec.IntValue BUTTON_Y;
    public static final ForgeConfigSpec.IntValue MAX_LEVEL_OVERRIDE;
    public static final ForgeConfigSpec.DoubleValue GEAR_SCREEN_HEIGHT;
    public static final ForgeConfigSpec.IntValue GEAR_SCREEN_WIDTH;
    public static final ForgeConfigSpec.DoubleValue CARD_SCREEN_HEIGHT;
    public static final ForgeConfigSpec.IntValue CARD_SCREEN_WIDTH;
    public static final ForgeConfigSpec.BooleanValue SHOW_ABILITY_ENHANCEMENTS;
    public static final ForgeConfigSpec.BooleanValue SHOW_WEIGHT;
    public static final ForgeConfigSpec.BooleanValue SHOW_CHANCE;
    public static final ForgeConfigSpec.BooleanValue QOL_HUNTERS_CONFLICT_RESOLUTION;
    public static final ForgeConfigSpec.BooleanValue SHOW_UNOBTAINABLE_CRAFTED;
    public static final ForgeConfigSpec.BooleanValue DEBUG_UNIQUE_GEAR;
    public static final ForgeConfigSpec.BooleanValue DEBUG_CARDS;
    // string instead of enum, because forge would remove enum values that are not present in the enum
    // (this could cause problems if mods are extending the enum - like wold's)
    public static final ForgeConfigSpec.ConfigValue<List<String>> AFFIX_TAG_GROUP_CHANCE_BLACKLIST;


    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

      builder.push("BUTTON");
        VAULT_SCREEN_BUTTON = builder
            .comment("open VHat can I roll? from vault screen")
            .define("vaultScreenButton", true);
        BUTTON_X = builder
            .comment("x position of the button")
            .defineInRange("buttonPositionX", 5, Integer.MIN_VALUE, Integer.MAX_VALUE);
        
        BUTTON_Y = builder
            .comment("y position of the button (109 default, 130 for QOL Hunters)")
            .defineInRange("buttonPositionY", 109, Integer.MIN_VALUE, Integer.MAX_VALUE);
        builder.pop();

        builder.push("DEBUG");
        QOL_HUNTERS_CONFLICT_RESOLUTION = builder
            .comment("QOL Hunters conflict resolution (shouldn't be disabled unless it causes issues)")
            .define("QOLHuntersConflictResolution", true);
        AFFIX_TAG_GROUP_CHANCE_BLACKLIST = builder
            .comment("vhcir won't show chance/weight for affixes in these groups")
            .define("affixTagGroupBlacklist", List.of(VaultGearTierConfig.ModifierAffixTagGroup.CRAFTED_PREFIX.name(), VaultGearTierConfig.ModifierAffixTagGroup.CRAFTED_SUFFIX.name()));
        MAX_LEVEL_OVERRIDE = builder
            .comment("override max level")
            .defineInRange("maxLevelOverride", -1, -1, Integer.MAX_VALUE);
        DEBUG_UNIQUE_GEAR = builder
            .comment("debug unique gear")
            .define("debugUniqueGear", false);
        DEBUG_CARDS = builder
            .comment("debug cards")
            .define("debugCards", false);
        builder.pop();

        SHOW_ABILITY_ENHANCEMENTS = builder
            .comment("show ability enhancements")
            .define("showAbilityEnhancements", false);

        SHOW_WEIGHT = builder
            .comment("show weight")
            .define("showWeight", false);

        SHOW_CHANCE = builder
            .comment("show chance")
            .define("showChance", true);

        SHOW_UNOBTAINABLE_CRAFTED = builder
            .comment("show unobtainable crafted modifiers (above current lvl)")
            .define("showUnobtainableCrafted", false);

        GEAR_SCREEN_HEIGHT = builder
            .comment("width of the gear screen")
            .defineInRange("screenHeight", 0.95, 0, 1.0);

        GEAR_SCREEN_WIDTH = builder
            .comment("width of the gear screen")
            .defineInRange("screenWidth", 370, 250, Integer.MAX_VALUE);

        CARD_SCREEN_HEIGHT = builder
            .comment("width of the card screen")
            .defineInRange("cardScreenHeight", 0.95, 0, 1.0);

        CARD_SCREEN_WIDTH = builder
            .comment("width of the card screen")
            .defineInRange("cardScreenWidth", 450, 250, Integer.MAX_VALUE);
        SPEC = builder.build();
    }
}
