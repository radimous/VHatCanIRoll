package com.radimous.vhatcaniroll.ui.artisan;

import com.radimous.vhatcaniroll.logic.GroupTextComponent;
import com.radimous.vhatcaniroll.logic.ModifierCategory;
import com.radimous.vhatcaniroll.logic.Modifiers;
import iskallia.vault.client.gui.framework.element.ElasticContainerElement;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.ModifierListElement;
import iskallia.vault.client.gui.framework.render.TooltipDirection;
import iskallia.vault.client.gui.framework.render.Tooltips;
import iskallia.vault.client.gui.framework.render.spi.ITooltipRenderFunction;
import iskallia.vault.client.gui.framework.screen.layout.ScreenLayout;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import iskallia.vault.config.gear.VaultGearTierConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.List;

public class ArtisanReplacement {
    public static void addModifierList(int itemLevel, VaultGearTierConfig config, ModifierListElement<?> modifierListElement, ElasticContainerElement<?> innerContainerElement) {
        int labelY = 0;
        int labelX = 2;
        List<Component> modifierComponents = Modifiers.getModifierList(itemLevel, config, ModifierCategory.NORMAL, true, VaultGearTierConfig.ModifierAffixTagGroup.IMPLICIT, VaultGearTierConfig.ModifierAffixTagGroup.PREFIX, VaultGearTierConfig.ModifierAffixTagGroup.SUFFIX);
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
