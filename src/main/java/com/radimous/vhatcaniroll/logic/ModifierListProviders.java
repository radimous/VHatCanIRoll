package com.radimous.vhatcaniroll.logic;

import com.radimous.vhatcaniroll.api.ModifierListProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/** Modifier list providers registered by other mods over IMC, asked in registration order. */
public class ModifierListProviders {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final List<Registration> PROVIDERS = new ArrayList<>();

    private record Registration(String modid, ModifierListProvider provider) {
    }

    public static void register(String sender, ModifierListProvider provider) {
        PROVIDERS.add(new Registration(sender, provider));
    }

    /** The first provider's lines for this stack, or null when no provider handles it. */
    @Nullable
    public static List<Component> find(ItemStack stack, int level, boolean mythic) {
        for (Registration registration : PROVIDERS) {
            try {
                List<Component> lines = registration.provider().modifiers(stack, level, mythic);
                if (lines != null) {
                    return lines;
                }
            } catch (RuntimeException e) {
                LOGGER.error("Modifier list provider from {} failed for {}", registration.modid(), stack.getItem().getRegistryName(), e);
            }
        }
        return null;
    }
}
