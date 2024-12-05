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
    public static final ForgeConfigSpec.BooleanValue SHOW_ABILITY_ENHANCEMENTS;    
    public static final ForgeConfigSpec.BooleanValue SHOW_WEIGHT;
    public static final ForgeConfigSpec.BooleanValue SHOW_CHANCE;
    public static final ForgeConfigSpec.BooleanValue QOL_HUNTERS_CONFLICT_RESOLUTION;
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

        AFFIX_TAG_GROUP_CHANCE_BLACKLIST = builder
            .comment("vhcir won't show chance/weight for affixes in these groups")
            .define("affixTagGroupBlacklist", List.of(VaultGearTierConfig.ModifierAffixTagGroup.CRAFTED_PREFIX.name(), VaultGearTierConfig.ModifierAffixTagGroup.CRAFTED_SUFFIX.name()));

        SPEC = builder.build();
    }
}
