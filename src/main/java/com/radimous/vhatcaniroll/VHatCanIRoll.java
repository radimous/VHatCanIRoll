package com.radimous.vhatcaniroll;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod("vhatcaniroll")
public class VHatCanIRoll {

    public static final String MODID = "vhatcaniroll";
    public VHatCanIRoll() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        MinecraftForge.EVENT_BUS.register(this);
    }
}
