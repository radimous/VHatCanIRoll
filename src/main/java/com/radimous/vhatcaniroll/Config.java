package com.radimous.vhatcaniroll;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue VAULT_SCREEN_BUTTON;
    public static final ForgeConfigSpec.BooleanValue ALLOW_DUPE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        VAULT_SCREEN_BUTTON = builder
            .comment("open VHat can I roll? from vault screen")
            .define("vaultScreenButton", true);

        builder.push("DEBUG");
        ALLOW_DUPE = builder
            .comment("allow duplicate modifiers")
            .define("allowDupe", false);
        builder.pop();

        SPEC = builder.build();
    }
}
