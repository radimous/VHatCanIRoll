package com.radimous.vhatcaniroll;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod("vhatcaniroll")
public class VHatCanIRoll {

    public static final String MODID = "vhatcaniroll";
    public static final Style ERROR_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_RED).withUnderlined(true).withItalic(true);

    public VHatCanIRoll() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        MinecraftForge.EVENT_BUS.register(this);
    }
}
