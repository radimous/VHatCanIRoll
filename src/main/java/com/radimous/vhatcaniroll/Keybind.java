package com.radimous.vhatcaniroll;

import com.mojang.blaze3d.platform.InputConstants;
import com.radimous.vhatcaniroll.ui.GearModifierScreen;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = VHatCanIRoll.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class Keybind {
    @SubscribeEvent
    public static void handleEventInput(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || event.phase == TickEvent.Phase.START) {
            return;
        }
        if (OPEN_MOD_SCREEN.consumeClick()) {
            mc.setScreen(new GearModifierScreen());
        }
    }


    public static final String VHAT_CAN_I_ROLL_CATEGORY = "key.categories.vhatcaniroll";
    public static final KeyMapping
        OPEN_MOD_SCREEN = new KeyMapping("vhatcaniroll.openmodscreen", KeyConflictContext.IN_GAME, InputConstants.UNKNOWN,
        VHAT_CAN_I_ROLL_CATEGORY);
}
