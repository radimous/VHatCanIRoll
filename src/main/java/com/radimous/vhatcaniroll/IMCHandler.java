package com.radimous.vhatcaniroll;


import com.mojang.datafixers.util.Pair;
import com.radimous.vhatcaniroll.api.ModifierListProvider;
import com.radimous.vhatcaniroll.logic.Items;
import com.radimous.vhatcaniroll.logic.ModifierListProviders;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = VHatCanIRoll.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class IMCHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void processIMC(InterModProcessEvent event) {

        event.getIMCStream().forEach(message -> {

            String sender = message.senderModId();
            String method = message.method();
            Object data = message.messageSupplier().get();

            switch (method) {
                case "register_items": // expects List<ItemStack>
                    handleRegisterItems(sender, data);
                    break;
                case "register_modifier_list": // expects ModifierListProvider
                    handleRegisterModifierList(sender, data);
                    break;
                default:
                    LOGGER.warn("Unknown IMC method: {}", method);
                    break;
            }
        });
    }

    private static void handleRegisterItems(String sender, Object data) {

        if (data instanceof Pair<?, ?> pair
                && pair.getFirst() instanceof Integer priority
                && pair.getSecond() instanceof List<?> items) {

            List<ItemStack> itemList = new ArrayList<>();
            for (Object obj : items) {

                if (obj instanceof ItemStack) {
                    ItemStack stack = ((ItemStack) obj).copy();
                    LOGGER.debug("Received: {} from {}", stack.getItem().getRegistryName(), sender);
                    itemList.add(stack);
                } else {
                    LOGGER.warn("Unknown IMC item: {}", obj.getClass().getName());
                }
            }
            Items.registerIMCItems(sender, priority, itemList);
        } else {
            LOGGER.warn("Invalid IMC format, register_items accepts Pair<Integer, List<ItemStack>>");
        }
    }

    private static void handleRegisterModifierList(String sender, Object data) {
        if (data instanceof ModifierListProvider provider) {
            LOGGER.debug("Received modifier list provider from {}", sender);
            ModifierListProviders.register(sender, provider);
        } else {
            LOGGER.warn("Invalid IMC format, register_modifier_list accepts ModifierListProvider");
        }
    }
}
