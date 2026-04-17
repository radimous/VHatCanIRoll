package com.radimous.vhatcaniroll.ui.sealer;

import com.radimous.vhatcaniroll.Config;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.NineSliceButtonElement;
import iskallia.vault.client.gui.framework.render.Tooltips;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.screen.block.VaultSealerScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SealerButton {
    public static NineSliceButtonElement<?> create(VaultSealerScreen sealerScreen, SealerModifierListElement<?> modifierListElement) {
        var btn = new NineSliceButtonElement(Spatials.positionXY(modifierListElement.getCalculatedWidth() + 180 - 7, 4).size(12, 12), ScreenTextures.BUTTON_EMPTY_TEXTURES, () -> {}){
            @Override public boolean onMouseScrolled(double mouseX, double mouseY, double delta) {
                int diff = delta > 0 ? 1 : -1;
                int newWidth = Config.SEALER_MODIFIER_LIST_WIDTH.get() + diff;
                if (newWidth < 100) {
                    return false;
                }
                Config.SEALER_MODIFIER_LIST_WIDTH.set(Config.SEALER_MODIFIER_LIST_WIDTH.get() + diff);
                Config.SEALER_MODIFIER_LIST_WIDTH.save();
                ItemStack gearStack = sealerScreen.getMenu().getGearInputSlot().getItem();
                if (gearStack.isEmpty()) {
                    gearStack = sealerScreen.getMenu().getOutputSlot().getItem();
                }
                modifierListElement.updateModifiers(gearStack);
                return true;
            }
        };
        btn.layout((screen, gui, parent, world) -> {
            world.positionX(modifierListElement.getCalculatedWidth() + 180 - 7);
            world.translateXY(gui);
        });
        btn.setVisible(modifierListElement::isVisible);
        btn.tooltip(Tooltips.multi(() -> List.of(new TextComponent("Scroll to adjust width").withStyle(ChatFormatting.GRAY))));

        return btn;
    }
}
