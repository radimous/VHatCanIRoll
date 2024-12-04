package com.radimous.vhatcaniroll;

import net.minecraftforge.common.ForgeConfigSpec;

public class QOLHuntersCompat {

    private static boolean qolHuntersLoaded = true;
    private static ForgeConfigSpec.BooleanValue qolConfigButton = null;

    public static void resolveQOLHuntersButtonConflict(){
        if(!Config.QOL_HUNTERS_CONFLICT_RESOLUTION.get()){
            // just for debugging or if it blows up
            return;
        }

        if (!qolHuntersLoaded) {
            if (Config.BUTTON_Y.get() == 130) {
                Config.BUTTON_Y.set(109);
            }
            return;
        }
        if (qolConfigButton == null) {
            // use reflection to avoid a million dependencies
            try {
                var cl = Class.forName("io.iridium.qolhunters.config.QOLHuntersClientConfigs");
                var qolButton = cl.getField("SHOW_CONFIG_BUTTON").get(null);
                qolConfigButton = (ForgeConfigSpec.BooleanValue) qolButton;
            } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
                qolHuntersLoaded = false;
            }
        }
        // if qol button, move our button down
        if (qolConfigButton != null && qolConfigButton.get() && Config.BUTTON_Y.get() == 109) {
            Config.BUTTON_Y.set(130);
        }
        // if no qol button, move our button up
        if ((qolConfigButton == null || !qolConfigButton.get()) && Config.BUTTON_Y.get() == 130) {
            Config.BUTTON_Y.set(109);
        }
    }
}
