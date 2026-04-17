package com.radimous.vhatcaniroll.ui.artisan;

import com.radimous.vhatcaniroll.Config;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.ModifierListElement;
import iskallia.vault.client.gui.framework.element.NineSliceButtonElement;
import iskallia.vault.client.gui.framework.render.Tooltips;
import iskallia.vault.client.gui.framework.screen.layout.ScreenLayout;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.screen.block.VaultArtisanStationScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;

import java.util.List;

public class ArtisanButton {
    public static NineSliceButtonElement<?> create(VaultArtisanStationScreen artisanStationScreen, ModifierListElement<?> modifierListElement) {
        var btn = new NineSliceButtonElement(Spatials.positionXY(modifierListElement.getCalculatedWidth() + 180 - 7, 4).size(12, 12), ScreenTextures.BUTTON_EMPTY_TEXTURES,
        () -> toggle(artisanStationScreen, modifierListElement)){
            @Override public boolean onMouseScrolled(double mouseX, double mouseY, double delta) {
                int diff = delta > 0 ? 1 : -1;
                int newWidth = Config.ARTISAN_MODIFIER_LIST_WIDTH.get() + diff;
                if (newWidth < 100) {
                    return false;
                }
                Config.ARTISAN_MODIFIER_LIST_WIDTH.set(Config.ARTISAN_MODIFIER_LIST_WIDTH.get() + diff);
                Config.ARTISAN_MODIFIER_LIST_WIDTH.save();
                modifierListElement.updateModifiers(artisanStationScreen.getMenu().getGearInputSlot().getItem());
                return true;
            }
        };
        btn.layout((screen, gui, parent, world) -> {
            world.positionX(modifierListElement.getCalculatedWidth() + 180 - 7);
            world.translateXY(gui);
        });
        btn.setVisible(modifierListElement::isVisible);
        btn.tooltip(Tooltips.multi(() -> List.of(
            new TextComponent("Toggle ").withStyle(ChatFormatting.GRAY)
            .append(
                new TextComponent("")
                .append(new TextComponent("vanilla").withStyle(Config.REPLACE_ARTISAN_MODIFIER_LIST.get() ? ChatFormatting.GRAY : ChatFormatting.WHITE))
                .append("/")
                .append(new TextComponent("custom").withStyle(Config.REPLACE_ARTISAN_MODIFIER_LIST.get() ? ChatFormatting.WHITE : ChatFormatting.GRAY))
                .append(" modifier list").withStyle(ChatFormatting.GRAY)
            ),
            new TextComponent("Scroll to adjust width").withStyle(ChatFormatting.GRAY))));

        return btn;
    }

    private static void toggle(VaultArtisanStationScreen artisanStationScreen, ModifierListElement<?> modifierListElement) {
        Config.REPLACE_ARTISAN_MODIFIER_LIST.set(!Config.REPLACE_ARTISAN_MODIFIER_LIST.get());
        Config.REPLACE_ARTISAN_MODIFIER_LIST.save();
        modifierListElement.updateModifiers(artisanStationScreen.getMenu().getGearInputSlot().getItem());
        ScreenLayout.requestLayout();
    }
}
