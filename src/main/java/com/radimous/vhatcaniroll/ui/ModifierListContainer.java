package com.radimous.vhatcaniroll.ui;

import com.radimous.vhatcaniroll.logic.ModifierCategory;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.VerticalScrollClipContainer;
import iskallia.vault.client.gui.framework.spatial.Padding;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.init.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

import com.radimous.vhatcaniroll.logic.Modifiers;

public class ModifierListContainer extends VerticalScrollClipContainer<ModifierListContainer> implements InnerGearScreen {

    public ModifierListContainer(ISpatial spatial, int lvl, ModifierCategory modifierCategory, ItemStack gearPiece) {
        super(spatial, Padding.ZERO, ScreenTextures.INSET_BLACK_BACKGROUND);
        int labelX = 9;
        int labelY = 20;

        // Label for the item name and level (GOLD if legendary, AQUA if greater, WHITE if common)
        LabelElement<?> itemName = new LabelElement<>(
            Spatials.positionXY(labelX, 5).width(this.innerWidth() - labelX).height(15), new TextComponent(gearPiece.getItem().getName(gearPiece).getString().toUpperCase() + " - LVL " + lvl)
            .withStyle(ChatFormatting.UNDERLINE).withStyle(modifierCategory.getStyle()), LabelTextStyle.defaultStyle()
        );
        this.addElement(itemName);

        List<Component> modifiers = null;
        if (gearPiece.getItem() == ModItems.VAULT_GOD_CHARM) {
            Optional<VaultGod> godOpt = VaultGearData.read(gearPiece).getFirstValue(ModGearAttributes.CHARM_VAULT_GOD);
            if (godOpt.isEmpty()) {
                modifiers = List.of(new TextComponent("Charm missing god attribution during modifier generation"));
            } else {
                VaultGod god = godOpt.get();
                modifiers = Modifiers.getAffixGroupComponents(lvl, VaultGearTierConfig.ModifierAffixTagGroup.PREFIX,  ModConfigs.VAULT_CHARM.getModifierGroup(god), ModifierCategory.NORMAL);
                LabelElement<?> itemNamePerRep = new LabelElement<>(
                    Spatials.positionXY(itemName.right(), 5).width(this.innerWidth() - labelX).height(15), new TextComponent("  values scale with " + god.getName() + " reputation")
                    .withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.DARK_GRAY), LabelTextStyle.defaultStyle()
                );
                this.addElement(itemNamePerRep);
            }
        }

        Optional<VaultGearTierConfig> optCfg = VaultGearTierConfig.getConfig(gearPiece);
        if (optCfg.isPresent()) {
            VaultGearTierConfig cfg = optCfg.get();
            modifiers = Modifiers.getModifierList(lvl, cfg, modifierCategory);
        }

        if (modifiers == null || modifiers.isEmpty()) {
            LabelElement<?> labelelement = new LabelElement<>(
                Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15), new TextComponent(
                "Modifier config for " + gearPiece.getItem() + " not found"), LabelTextStyle.defaultStyle()
            );
            this.addElement(labelelement);
            return;
        }

        for (Component modifier : modifiers) {
            LabelElement<?> labelelement = new LabelElement<>(
                Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15), modifier, LabelTextStyle.defaultStyle()
            );
            this.addElement(labelelement);
            labelY += 10;
                /*  TODO: display individual modifier tiers
                    I want to display
                    <groupPrefix> <value> <name> <chance>
                    and make it expandable to show all tiers of the modifier

                    <groupPrefix> <value> <name> <chance>
                        <groupPrefix> <value> <name> <chance>
                        <groupPrefix> <value> <name> <chance>
                        <groupPrefix> <value> <name> <chance>
                */

        }
    }
    public float getScroll() {
        return this.verticalScrollBarElement.getValue();
    }

    public void setScroll(float scroll) {
        this.verticalScrollBarElement.setValue(scroll);
    }

    @Override
    public InnerGearScreen create(ISpatial spatial, int lvl, ModifierCategory modifierCategory, ItemStack gearPiece) {
        return new ModifierListContainer(spatial, lvl, modifierCategory, gearPiece);
    }
}