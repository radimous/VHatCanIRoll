package com.radimous.vhatcaniroll.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import com.radimous.vhatcaniroll.Config;
import com.radimous.vhatcaniroll.VHatCanIRoll;
import com.radimous.vhatcaniroll.logic.Items;
import com.radimous.vhatcaniroll.ui.gear.GearModifierScreen;

import iskallia.vault.VaultMod;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.gear.VaultCharmItem;
import iskallia.vault.item.tool.ToolItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.*;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static net.minecraft.util.Mth.hsvToRgb;

@Mod.EventBusSubscriber(modid = VHatCanIRoll.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class Keybind {

    private static final int HOLD_THRESHOLD = 20;
    private static final int totalBars = 10;

    private static int holdCounter = 0;


    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (mc.screen != null) {
            if (OPEN_MOD_SCREEN_HOVER.isDown()) {
                if (++holdCounter >= HOLD_THRESHOLD) {
                    openModifierScreen();
                    holdCounter = 0;
                }
            } else {
                holdCounter = 0;
            }
            return;
        }

        if (OPEN_MOD_SCREEN.consumeClick()) {
            openModifierScreen();
        }
    }

    @SubscribeEvent
    public static void onTooltipRender(RenderTooltipEvent.GatherComponents event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.screen == null || !Config.SHOW_HOVER_TOOLTIP.get()) {
            return;
        }

        ItemStack hoverStack = event.getItemStack();
        if (!isValidItem(hoverStack.getItem())) {
            return;
        }

        Component tooltip;

        if (isValidItem(hoverStack.getItem())) {
            if(holdCounter > 0) {
                double progress = Math.min(1.0, (double) holdCounter / HOLD_THRESHOLD);
                tooltip = createProgressBar(progress);
            }
            else {
                tooltip = new TranslatableComponent(
                        "vhatcaniroll.key.hover_tooltip",
                        new TextComponent("[")
                                .append(OPEN_MOD_SCREEN_HOVER.getTranslatedKeyMessage().copy().withStyle(ChatFormatting.GRAY))
                                .append("]")
                ).withStyle(ChatFormatting.DARK_GRAY);
            }

            event.getTooltipElements().add(Either.left(tooltip));
        }
    }

    private static ItemStack getHoveredItem(Screen screen) {
        if(screen instanceof AbstractContainerScreen<?> containerScreen) {
            Slot slot = containerScreen.getSlotUnderMouse();
            if(slot != null) {
                return slot.getItem();
            }
        }

        return ItemStack.EMPTY;
    }

    private static void openModifierScreen() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.screen == null) {
            mc.setScreen(new GearModifierScreen());
            return;
        }

        ItemStack hoverStack = getHoveredItem(mc.screen);
        if (!isValidItem(hoverStack.getItem())) {
            return;
        }

        VaultGearData hoverData = VaultGearData.read(hoverStack);
        Optional<VaultGod> hoverGodCharm = hoverData.getFirstValue(ModGearAttributes.CHARM_VAULT_GOD);

        List<ItemStack> vaultGearItems = Items.getVaultGearItems();

        int targetIndex = IntStream.range(0, vaultGearItems.size())
                .filter(i -> {
                    ItemStack tabItem = vaultGearItems.get(i);

                    if(tabItem.getItem() instanceof VaultCharmItem) {
                        VaultGearData tabData = VaultGearData.read(tabItem);
                        Optional<VaultGod> tabGodCharm = tabData.getFirstValue(ModGearAttributes.CHARM_VAULT_GOD);

                        if (hoverGodCharm.isPresent() && tabGodCharm.isPresent()) {
                            return hoverGodCharm.get().equals(tabGodCharm.get());
                        }
                    }

                    return hoverStack.getItem().equals(tabItem.getItem());
                })
                .findFirst()
                .orElse(0);

        GearModifierScreen modifierScreen = GearModifierScreen.openScreenAtIndex(targetIndex);

        modifierScreen.setLevelInput(hoverData.getItemLevel());

        if(hoverData.getFirstValue(ModGearAttributes.GEAR_UNIQUE_POOL).isPresent()) {
            modifierScreen.switchToUnique();
        }

        if (VaultMod.id("map").equals(hoverStack.getItem().getRegistryName())){
            for (var mapAttr: hoverData.getAttributes()) {
                if (VaultMod.id("map_tier").equals(mapAttr.getAttribute().getRegistryName()) && mapAttr.getValue() instanceof Integer intValue) {
                    modifierScreen.setLevelInput(intValue);
                }
            }
        }
        if ("MYTHIC".equals(hoverData.getRarity().name())) {
            modifierScreen.setMythic(true);
        }

        mc.setScreen(modifierScreen);
    }

    private static boolean isValidItem(Item item) {
        return (item instanceof VaultGearItem && !(item instanceof ToolItem));
    }

    private static Component createProgressBar(double progress) {
        int filled = (int) Math.round(progress * totalBars);

        TextComponent bar = new TextComponent("[");
        bar.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));

        for (int i = 0; i < totalBars; i++) {
            TextComponent segment = new TextComponent("=");

            if (i < filled) {
                float hue = (float) i / totalBars;
                int rgb = hsvToRgb(hue, 1.0f, 1.0f);
                segment.setStyle(Style.EMPTY.withColor(rgb));
            } else {
                segment.setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY));
            }

            bar.append(segment);
        }

        bar.append(new TextComponent("]")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));

        return bar;
    }




    public static final String VHAT_CAN_I_ROLL_CATEGORY = "key.categories.vhatcaniroll";
    public static final KeyMapping OPEN_MOD_SCREEN = new KeyMapping("vhatcaniroll.openmodscreen", KeyConflictContext.IN_GAME, InputConstants.UNKNOWN, VHAT_CAN_I_ROLL_CATEGORY);
    public static final KeyMapping OPEN_MOD_SCREEN_HOVER = new KeyMapping("vhatcaniroll.openmodscreen_hover", KeyConflictContext.GUI, InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_W), VHAT_CAN_I_ROLL_CATEGORY);
}
