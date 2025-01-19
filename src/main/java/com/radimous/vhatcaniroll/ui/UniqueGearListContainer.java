package com.radimous.vhatcaniroll.ui;

import com.radimous.vhatcaniroll.logic.ModifierCategory;
import com.radimous.vhatcaniroll.logic.Modifiers;
import com.radimous.vhatcaniroll.mixin.UniqueGearConfigAccessor;
import com.radimous.vhatcaniroll.mixin.VaultGearTierConfigAccessor;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.VerticalScrollClipContainer;
import iskallia.vault.client.gui.framework.spatial.Padding;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import iskallia.vault.config.UniqueGearConfig;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.init.ModConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UniqueGearListContainer extends VerticalScrollClipContainer<UniqueGearListContainer> implements InnerGearScreen {

    public UniqueGearListContainer(ISpatial spatial, int lvl, ModifierCategory modifierCategory, ItemStack gearPiece) {
        super(spatial, Padding.ZERO, ScreenTextures.INSET_BLACK_BACKGROUND);
        int labelX = 9;
        int labelY = 20;


        // Label for the item name and level (GOLD if legendary, AQUA if greater, WHITE if common)
        LabelElement<?> itemName = new LabelElement<>(
            Spatials.positionXY(labelX, 5).width(this.innerWidth() - labelX).height(15), new TextComponent(
            gearPiece.getItem().toString().toUpperCase() + " - LVL " + lvl)
            .withStyle(ChatFormatting.UNDERLINE).withStyle(modifierCategory.getStyle()), LabelTextStyle.defaultStyle()
        );
        this.addElement(itemName);

        Map<ResourceLocation, UniqueGearConfig.Entry> uniqueRegistry = ((UniqueGearConfigAccessor) ModConfigs.UNIQUE_GEAR).getRegistry();
        var regName = gearPiece.getItem().getRegistryName();
        if (regName == null) {
            return;
        }
        var regPath = regName.getPath();

        var goodEntries = uniqueRegistry.entrySet().stream().filter(entry -> entry.getValue().getModel() != null && entry.getValue().getModel().toString().contains(regPath)).toList();
        List<Map.Entry<ResourceLocation, UniqueGearConfig.Entry>> badEntries = uniqueRegistry.entrySet().stream().filter(entry -> entry.getValue().getModel() == null || !entry.getValue().getModel().toString().contains(regPath)).toList();

        var uniqueConfig1 = (VaultGearTierConfigAccessor) ModConfigs.VAULT_GEAR_CONFIG.get(VaultGearTierConfig.UNIQUE_ITEM);
        if (uniqueConfig1 == null) {
            return;
        }
        for (var entry : goodEntries) {
            var value = entry.getValue();
            ResourceLocation id = value.getId() == null ? new ResourceLocation("minecraft", "missing") : value.getId();
            String name = value.getName() == null ? "missing" : value.getName();
            ResourceLocation model = value.getModel() == null ? new ResourceLocation("minecraft", "missing") : value.getModel();

            List<String> modifierTags = value.getModifierTags() == null ? List.of("missing") : value.getModifierTags();
            Map<UniqueGearConfig.AffixTargetType, List<ResourceLocation>> modifierIdentifiers = value.getModifierIdentifiers() == null ? Map.of(UniqueGearConfig.AffixTargetType.PREFIX, List.of(new ResourceLocation("minecraft", "missing"))) : value.getModifierIdentifiers();

            LabelElement<?> idLabel = new LabelElement<>(
                Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15),
                new TextComponent(
                    "ID: " + id.toString()), LabelTextStyle.defaultStyle()
            );
            this.addElement(idLabel);
            labelY += 10;
            LabelElement<?> nameLabel = new LabelElement<>(
                Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15),
                new TextComponent(
                    "Name: " + name), LabelTextStyle.defaultStyle()
            );
            this.addElement(nameLabel);
            labelY += 10;
            LabelElement<?> modelLabel = new LabelElement<>(
                Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15),
                new TextComponent(
                    "Model: " + model.toString()), LabelTextStyle.defaultStyle()
            );
            this.addElement(modelLabel);
            labelY += 10;
            LabelElement<?> modifierTagsLabel = new LabelElement<>(
                Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15),
                new TextComponent(
                    "Modifier Tags: " + modifierTags.toString()), LabelTextStyle.defaultStyle()
            );
            this.addElement(modifierTagsLabel);
            labelY += 10;
            for (Map.Entry<UniqueGearConfig.AffixTargetType, List<ResourceLocation>> modifierIdentifier : modifierIdentifiers.entrySet()) {
                if (modifierIdentifier.getValue().isEmpty()) {
                    continue;
                }
                this.addElement(new LabelElement<>(
                    Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15),
                    new TextComponent(modifierIdentifier.getKey().toString()), LabelTextStyle.defaultStyle()));
                labelY += 10;
                for (ResourceLocation modifier : modifierIdentifier.getValue()) {
                    Map<VaultGearTierConfig.ModifierAffixTagGroup, VaultGearTierConfig.AttributeGroup> ff = uniqueConfig1.getModifierGroup();
                    List<VaultGearTierConfig.AttributeGroup> kk = new ArrayList<>();
                    for (Map.Entry<VaultGearTierConfig.ModifierAffixTagGroup, VaultGearTierConfig.AttributeGroup> f : ff.entrySet()) {
                        kk.add(f.getValue());
                    }
                    var someModTierGroup = kk.get(0).get(0);
                    var mTierList = Modifiers.getModifierTiers(lvl,someModTierGroup , modifierCategory);
                    var mc = Modifiers.getModifierComponent(VaultGearAttributeRegistry.getAttribute(modifier),mTierList);

                    //TODO: figure this out
//                    LabelElement<?> mcl = new LabelElement<>(
//                        Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15),
//                        mc, LabelTextStyle.defaultStyle());
//                    this.addElement(mcl);
                    labelY += 10;
                    LabelElement<?> modifierIdentifierLabel = new LabelElement<>(
                        Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15),
                        new TextComponent("  " + modifier.toString()), LabelTextStyle.defaultStyle());
                    this.addElement(modifierIdentifierLabel);
                    labelY += 10;
                }
                labelY += 10;
            }
            labelY += 10;

            this.addElement(new LabelElement<>(
                Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15),
                new TextComponent("----------------------------------------"), LabelTextStyle.defaultStyle()));
            labelY += 10;
        }
        this.addElement(new LabelElement<>(
            Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15),
            new TextComponent("BAD ENTRIES:").withStyle(ChatFormatting.RED), LabelTextStyle.defaultStyle()));
        labelY += 10;
        for (var entry : badEntries) {
            this.addElement(new LabelElement<>(
                Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15),
                new TextComponent("ID: " + entry.getKey().toString()).withStyle(ChatFormatting.RED), LabelTextStyle.defaultStyle()));
            labelY += 10;
            var vv = entry.getValue();
            if (vv == null) {
                continue;
            }
            this.addElement(new LabelElement<>(
                Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15),
                new TextComponent("Model: " + vv.getModel()).withStyle(ChatFormatting.RED), LabelTextStyle.defaultStyle()));
            labelY += 16;
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
        return new UniqueGearListContainer(spatial, lvl, modifierCategory, gearPiece);
    }
}