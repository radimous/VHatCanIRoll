package com.radimous.vhatcaniroll.ui;

import com.radimous.vhatcaniroll.logic.ModifierCategory;
import iskallia.vault.VaultMod;
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
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

import com.radimous.vhatcaniroll.logic.Modifiers;

public class ModifierListContainer extends VerticalScrollClipContainer<ModifierListContainer> implements InnerGearScreen {

    public ModifierListContainer(ISpatial spatial, int lvl, ModifierCategory modifierCategory, ItemStack gearPiece, boolean mythic) {
        super(spatial, Padding.ZERO, ScreenTextures.INSET_BLACK_BACKGROUND);
        int labelX = 9;
        int labelY = 20;

        // Label for the item name and level (GOLD if legendary, GREEN if greater, WHITE if common)
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
        var regName = gearPiece.getItem().getRegistryName();

        if (VaultMod.id("map").equals(regName)){
            int idx = mythic ? 0 : lvl % 10; // TODO: second input for tier?

            lvl = 100;
            if (idx != 0) {
                optCfg = Optional.ofNullable(ModConfigs.VAULT_GEAR_CONFIG.get(VaultMod.id("map_"+idx)));
            }
            itemName.set(new TextComponent(gearPiece.getItem().getName(gearPiece).getString().toUpperCase() + " - Tier " + idx)
                .withStyle(ChatFormatting.UNDERLINE).withStyle(modifierCategory.getStyle()));

        }
        if (regName != null && mythic){
            optCfg = Optional.ofNullable(ModConfigs.VAULT_GEAR_CONFIG.get(VaultMod.id(regName.getPath()+"_mythic")));
            var newComp = new TextComponent("");
            newComp.append(itemName.getComponent());
            newComp.append(new TextComponent(" Mythic").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(mythic ? 15597727 : ChatFormatting.WHITE.getColor()))));
            itemName.set(newComp);
        }
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

        for (Component mc : modifiers) {
            if (mc instanceof TextComponent tc){ // try to make wrapped text
                var newTc = new TextComponent("");
                for (var sibling: tc.getSiblings()){
                    newTc.append(sibling);
                }
                var gtc = new TextComponent(tc.getText()).withStyle(tc.getStyle());
                LabelElement<?> gcl = new LabelElement<>(Spatials.positionXY(labelX, labelY), gtc, LabelTextStyle.defaultStyle());
                this.addElement(gcl);

                LabelElement<?> mcl = new LabelElement<>(
                    Spatials.positionXY(labelX + gcl.width(), labelY).width(this.innerWidth() - labelX - gcl.width()),
                    Spatials.width(this.innerWidth() - labelX * 2).height(9),
                    newTc, LabelTextStyle.wrap());
                this.addElement(mcl);
                labelY += Math.max(mcl.getTextStyle().calculateLines(newTc, mcl.width()) * 10, 10);
            } else {
                LabelElement<?> labelelement = new LabelElement<>(
                    Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15), mc, LabelTextStyle.defaultStyle()
                );
                this.addElement(labelelement);
                labelY += 10;
            }

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
    public InnerGearScreen create(ISpatial spatial, int lvl, ModifierCategory modifierCategory, ItemStack gearPiece, boolean mythic) {
        return new ModifierListContainer(spatial, lvl, modifierCategory, gearPiece, mythic);
    }
}