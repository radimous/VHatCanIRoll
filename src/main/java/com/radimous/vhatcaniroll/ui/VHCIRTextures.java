package com.radimous.vhatcaniroll.ui;


import iskallia.vault.VaultMod;
import iskallia.vault.client.atlas.TextureAtlasRegion;
import iskallia.vault.client.gui.framework.element.ButtonElement;
import iskallia.vault.init.ModTextureAtlases;

public class VHCIRTextures {
    public static final TextureAtlasRegion BUTTON_HELP = TextureAtlasRegion.of(ModTextureAtlases.SCREEN, VaultMod.id("gui/screen/button/vhcir_help"));
    public static final TextureAtlasRegion BUTTON_HELP_DISABLED = TextureAtlasRegion.of(ModTextureAtlases.SCREEN, VaultMod.id("gui/screen/button/vhcir_help_disabled"));
    public static final TextureAtlasRegion BUTTON_HELP_HOVER = TextureAtlasRegion.of(ModTextureAtlases.SCREEN, VaultMod.id("gui/screen/button/vhcir_help_hover"));
    public static final TextureAtlasRegion BUTTON_HELP_PRESSED = TextureAtlasRegion.of(ModTextureAtlases.SCREEN, VaultMod.id("gui/screen/button/vhcir_help_pressed"));
    public static final ButtonElement.ButtonTextures HELP_BUTTON_TEXTURES = new ButtonElement.ButtonTextures(BUTTON_HELP, BUTTON_HELP_HOVER, BUTTON_HELP_PRESSED, BUTTON_HELP_DISABLED);

    public static final TextureAtlasRegion BUTTON_CONFIG = TextureAtlasRegion.of(ModTextureAtlases.SCREEN, VaultMod.id("gui/screen/button/vhcir_config"));
    public static final TextureAtlasRegion BUTTON_CONFIG_DISABLED = TextureAtlasRegion.of(ModTextureAtlases.SCREEN, VaultMod.id("gui/screen/button/vhcir_config_disabled"));
    public static final TextureAtlasRegion BUTTON_CONFIG_HOVER = TextureAtlasRegion.of(ModTextureAtlases.SCREEN, VaultMod.id("gui/screen/button/vhcir_config_hover"));
    public static final TextureAtlasRegion BUTTON_CONFIG_PRESSED = TextureAtlasRegion.of(ModTextureAtlases.SCREEN, VaultMod.id("gui/screen/button/vhcir_config_pressed"));
    public static final ButtonElement.ButtonTextures CONFIG_BUTTON_TEXTURES = new ButtonElement.ButtonTextures(BUTTON_CONFIG, BUTTON_CONFIG_HOVER, BUTTON_CONFIG_PRESSED, BUTTON_CONFIG_DISABLED);

}
