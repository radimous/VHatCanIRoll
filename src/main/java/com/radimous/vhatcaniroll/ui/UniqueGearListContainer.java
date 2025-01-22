package com.radimous.vhatcaniroll.ui;

import com.radimous.vhatcaniroll.Config;
import com.radimous.vhatcaniroll.logic.ModifierCategory;
import com.radimous.vhatcaniroll.logic.Modifiers;
import com.radimous.vhatcaniroll.mixin.UniqueGearConfigAccessor;
import com.radimous.vhatcaniroll.mixin.VaultGearTierConfigAccessor;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.FakeItemSlotElement;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.NineSliceElement;
import iskallia.vault.client.gui.framework.element.VerticalScrollClipContainer;
import iskallia.vault.client.gui.framework.spatial.Padding;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import iskallia.vault.config.UniqueGearConfig;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

public class UniqueGearListContainer extends VerticalScrollClipContainer<UniqueGearListContainer> implements InnerGearScreen {

    public UniqueGearListContainer(ISpatial spatial, int lvl, ModifierCategory modifierCategory, ItemStack gearPiece) {
        super(spatial, Padding.ZERO, ScreenTextures.INSET_BLACK_BACKGROUND);
        int labelX = 9;
        int labelY = 9;

        Map<ResourceLocation, UniqueGearConfig.Entry> uniqueRegistry = ((UniqueGearConfigAccessor) ModConfigs.UNIQUE_GEAR).getRegistry();
        ResourceLocation regName = gearPiece.getItem().getRegistryName();
        if (regName == null) {
            return;
        }
        String regPath = regName.getPath();

        var goodEntries = uniqueRegistry.entrySet().stream().filter(entry -> entry.getValue().getModel() != null && entry.getValue().getModel().toString().contains(regPath)).toList();


        VaultGearTierConfigAccessor uniqueConfig1 = (VaultGearTierConfigAccessor) ModConfigs.VAULT_GEAR_CONFIG.get(VaultGearTierConfig.UNIQUE_ITEM);
        if (uniqueConfig1 == null) {
            return;
        }
        for (Map.Entry<ResourceLocation, UniqueGearConfig.Entry> entry : goodEntries) {
            UniqueGearConfig.Entry value = entry.getValue();
            String name = value.getName();
            if (name == null) {
                continue;
            }
            ResourceLocation model = value.getModel();
            if (model == null) {
                continue;
            }

            Map<UniqueGearConfig.AffixTargetType, List<ResourceLocation>> modifierIdentifiers = value.getModifierIdentifiers();
            if (modifierIdentifiers == null) {
                continue;
            }
            int iconHeight = labelY;

            labelY += 5;
            LabelElement<?> nameLabel = new LabelElement<>(
                Spatials.positionXY(labelX + 20, labelY).width(this.innerWidth() - labelX).height(15),
                new TextComponent(name), LabelTextStyle.defaultStyle()
            );
            this.addElement(nameLabel);
            labelY += 20;

            ItemStack displayStack = new ItemStack(gearPiece.getItem());
            VaultGearData gearData = VaultGearData.read(displayStack);
            gearData.setState(VaultGearState.IDENTIFIED);
            gearData.createOrReplaceAttributeValue(ModGearAttributes.GEAR_MODEL, model);
            gearData.write(displayStack);
            this.addElement(new FakeItemSlotElement<>(Spatials.positionXY(labelX - 4, iconHeight).width(16).height(16), () -> displayStack, () -> false, ScreenTextures.EMPTY, ScreenTextures.EMPTY));


            List<Component> mlist = Modifiers.getUniqueModifierList(lvl, modifierCategory, modifierIdentifiers);
            for (Component mc : mlist) {
                LabelElement<?> mcl = new LabelElement<>(
                    Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15),
                    mc, LabelTextStyle.defaultStyle());
                this.addElement(mcl);
                labelY += 10;
            }
            this.addElement(new NineSliceElement<>(
                Spatials.positionXY(0, labelY).width(this.innerWidth()).height(3),
                ScreenTextures.BUTTON_EMPTY));
            labelY += 10;
        }
        if (Config.DEBUG_UNIQUE_GEAR.get()) {
            var badEntries = uniqueRegistry.entrySet().stream().filter(entry -> entry.getValue().getModel() == null || !entry.getValue().getModel().toString().contains(regPath)).toList();
            this.addElement(new LabelElement<>(
                Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15),
                new TextComponent("[DEBUG] BAD ENTRIES:").withStyle(ChatFormatting.RED), LabelTextStyle.defaultStyle()));
            labelY += 10;
            for (Map.Entry<ResourceLocation, UniqueGearConfig.Entry> entry : badEntries) {
                this.addElement(new LabelElement<>(
                    Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15),
                    new TextComponent("ID: " + entry.getKey().toString()).withStyle(ChatFormatting.RED),
                    LabelTextStyle.defaultStyle()));
                labelY += 10;
                UniqueGearConfig.Entry value = entry.getValue();
                if (value == null) {
                    continue;
                }
                this.addElement(new LabelElement<>(
                    Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15),
                    new TextComponent("Model: " + value.getModel()).withStyle(ChatFormatting.RED),
                    LabelTextStyle.defaultStyle()));
                labelY += 16;
            }
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