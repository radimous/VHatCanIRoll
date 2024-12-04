package com.radimous.vhatcaniroll.ui;

import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.VerticalScrollClipContainer;
import iskallia.vault.client.gui.framework.spatial.Padding;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import iskallia.vault.config.gear.VaultGearTierConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

import com.radimous.vhatcaniroll.logic.Modifiers;

public class ModifierListContainer extends VerticalScrollClipContainer<ModifierListContainer> {

    public ModifierListContainer(ISpatial spatial, int lvl, int tierIncrease, ItemStack gearPiece) {
        super(spatial, Padding.ZERO, ScreenTextures.INSET_BLACK_BACKGROUND);
        int labelX = 9;
        int labelY = 20;
        Optional<VaultGearTierConfig> optCfg = VaultGearTierConfig.getConfig(gearPiece);

        
        // Label for the item name and level (GOLD if legendary, AQUA if greater, WHITE if common)
        LabelElement<?> itemName = new LabelElement<>(
            Spatials.positionXY(labelX, 5).width(this.innerWidth() - labelX).height(15), new TextComponent(
                gearPiece.getItem().toString().toUpperCase() + " - LVL " + lvl)
                //TODO: make it nicer
            .withStyle(ChatFormatting.UNDERLINE).withStyle(tierIncrease == 2 ? ChatFormatting.GOLD : tierIncrease == 1 ? ChatFormatting.AQUA : ChatFormatting.WHITE), LabelTextStyle.defaultStyle()
        );
        this.addElement(itemName);


        if (optCfg.isPresent()) {
            VaultGearTierConfig cfg = optCfg.get();
            for (var modifier : Modifiers.getModifierList(lvl, cfg, tierIncrease)) {
                LabelElement<?> labelelement = new LabelElement<>(
                    Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15), modifier, LabelTextStyle.defaultStyle()
                );
                /*  TODO: maybe weight/chance should be added here, because there is everything you need for custom position
                    that would require returning something else from getModifierList (maybe List<Pair<Component, Component>>)
                    where first component is the modifier and second is the weight/chance or both (depending on the config)
                    also it should potentionally return a list of modifiers to get all tiers of the modifier
                    
                    
                    I want to display
                    <groupPrefix> <value> <name> <chance>
                    and make it expandable to show all tiers of the modifier
                    
                    <groupPrefix> <value> <name> <chance>
                        <groupPrefix> <value> <name> <chance>
                        <groupPrefix> <value> <name> <chance>
                        <groupPrefix> <value> <name> <chance>
                */
                this.addElement(labelelement);
                labelY += 10;
            }
        } else {
            LabelElement<?> labelelement = new LabelElement<>(
                Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15), new TextComponent(
                "Modifier config for " + gearPiece.getItem() + " not found"), LabelTextStyle.defaultStyle()
            );
            this.addElement(labelelement);
        }
    }
    public float getScroll() {
        return this.verticalScrollBarElement.getValue();
    }

    public void setScroll(float scroll) {
        this.verticalScrollBarElement.setValue(scroll);
    }
}