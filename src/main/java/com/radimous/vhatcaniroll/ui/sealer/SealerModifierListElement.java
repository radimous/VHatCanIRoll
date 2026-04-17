package com.radimous.vhatcaniroll.ui.sealer;

import com.radimous.vhatcaniroll.Config;
import com.radimous.vhatcaniroll.logic.GroupTextComponent;
import com.radimous.vhatcaniroll.logic.ModifierCategory;
import com.radimous.vhatcaniroll.logic.Modifiers;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.ElasticContainerElement;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.VerticalScrollClipContainer;
import iskallia.vault.client.gui.framework.render.NineSlice;
import iskallia.vault.client.gui.framework.render.TooltipDirection;
import iskallia.vault.client.gui.framework.render.Tooltips;
import iskallia.vault.client.gui.framework.render.spi.ITooltipRenderFunction;
import iskallia.vault.client.gui.framework.screen.layout.ScreenLayout;
import iskallia.vault.client.gui.framework.spatial.Padding;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.data.VaultGearData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class SealerModifierListElement<E extends SealerModifierListElement<E>> extends VerticalScrollClipContainer<E> {
    public SealerModifierListElement(ISpatial spatial) {
        super(spatial, Padding.of(2, 2, 2, 2), ScreenTextures.INSET_DARK_GREY_BACKGROUND);
    }

    public SealerModifierListElement(ISpatial spatial, Padding padding, NineSlice.TextureRegion background) {
        super(spatial, padding, background);
    }

    public int getCalculatedWidth() {
        int availableWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth() - this.left() - 14 /* frame width */;
        if (availableWidth < Config.SEALER_MODIFIER_LIST_WIDTH.get()) {
            return availableWidth;
        } else {
            return Config.SEALER_MODIFIER_LIST_WIDTH.get();
        }
    }

    public void updateModifiers(ItemStack gearStack) {
        this.innerContainerElement.getElementStore().removeAllElements();
        if (gearStack.isEmpty()) {
            this.addEmptyMessage("Place gear to view");
            ScreenLayout.requestLayout();
        } else {
            Optional<VaultGearTierConfig> configOpt = VaultGearTierConfig.getConfig(gearStack);
            if (configOpt.isEmpty()) {
                this.addEmptyMessage("No modifiers available");
                ScreenLayout.requestLayout();
            } else {
                VaultGearTierConfig config = configOpt.get();
                int itemLevel = this.getItemLevel(gearStack);
                addModifierList(itemLevel, config, this, this.innerContainerElement);

                ScreenLayout.requestLayout();
            }
        }
    }

    private void addEmptyMessage(String message) {
        LabelElement<?> label = new LabelElement<>(Spatials.positionXY(4, 4), (new TextComponent(message)).withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true)), LabelTextStyle.defaultStyle());
        this.innerContainerElement.getElementStore().addElement(label);
    }

    private int getItemLevel(ItemStack stack) {
        if (!AttributeGearData.hasData(stack)) {
            return 0;
        } else {
            AttributeGearData data = AttributeGearData.read(stack);
            if (data instanceof VaultGearData vaultGearData) {
                return vaultGearData.getItemLevel();
            } else {
                return 0;
            }
        }
    }


    public static void addModifierList(int itemLevel, VaultGearTierConfig config, SealerModifierListElement<?> modifierListElement, ElasticContainerElement<?> innerContainerElement) {
        int labelY = 0;
        int labelX = 2;
        List<Component> modifierComponents = Modifiers.getModifierList(itemLevel, config, ModifierCategory.NORMAL,false, VaultGearTierConfig.ModifierAffixTagGroup.CORRUPTED_IMPLICIT);
        for (Component mc : modifierComponents) {
            if (mc instanceof TextComponent tc) { // try to make wrapped text
                List<Component> groupTooltip;
                if (mc instanceof GroupTextComponent groupTextComponent) {
                    tc = groupTextComponent.getTextComponent();
                    groupTooltip = groupTextComponent.getGroupTooltip();
                } else {
                    groupTooltip = null;
                }
                var newTc = new TextComponent("");
                for (var sibling : tc.getSiblings()) {
                    newTc.append(sibling);
                }
                var gtc = new TextComponent(tc.getText()).withStyle(tc.getStyle());
                LabelElement<?> gcl = new LabelElement<>(Spatials.positionXY(labelX, labelY), gtc, LabelTextStyle.defaultStyle());
                if (groupTooltip != null) {
                    gcl.tooltip(Tooltips.shift(ITooltipRenderFunction.NONE, Tooltips.multi(TooltipDirection.LEFT, () -> groupTooltip)));
                }
                innerContainerElement.getElementStore().addElement(gcl);

                LabelElement<?> mcl = new LabelElement<>(
                    Spatials.positionXY(labelX + gcl.width(), labelY).width(modifierListElement.innerWidth() - labelX - gcl.width()),
                    Spatials.width(modifierListElement.innerWidth() - labelX * 2 - 6).height(9),
                    newTc, LabelTextStyle.wrap());
                innerContainerElement.getElementStore().addElement(mcl);
                labelY += Math.max(mcl.getTextStyle().calculateLines(newTc, mcl.width()) * 10, 10);
            } else {
                LabelElement<?> labelelement = new LabelElement<>(
                    Spatials.positionXY(labelX, labelY).width(modifierListElement.innerWidth() - labelX).height(15), mc, LabelTextStyle.defaultStyle()
                );
                innerContainerElement.getElementStore().addElement(labelelement);
                labelY += 10;
            }
        }
    }

}