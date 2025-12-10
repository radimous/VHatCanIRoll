package com.radimous.vhatcaniroll.ui.gear.inner;

import com.radimous.vhatcaniroll.logic.ModifierCategory;
import iskallia.vault.block.TransmogTableBlock;
import iskallia.vault.client.data.ClientDiscoveredEntriesData;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.DiscoveredModelSelectElement;
import iskallia.vault.client.gui.framework.element.FakeItemSlotElement;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.NineSliceButtonElement;
import iskallia.vault.client.gui.framework.element.VerticalScrollClipContainer;
import iskallia.vault.client.gui.framework.spatial.Padding;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModDynamicModels;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.util.SideOnlyFixer;
import iskallia.vault.util.function.ObservableSupplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.text.WordUtils;

import java.util.List;
import java.util.Set;

import static iskallia.vault.client.gui.framework.ScreenTextures.BUTTON_EMPTY;
import static iskallia.vault.client.gui.framework.ScreenTextures.BUTTON_EMPTY_DISABLED;

public class TransmogListContainer extends VerticalScrollClipContainer<TransmogListContainer> implements InnerGearScreen {

    public TransmogListContainer(ISpatial spatial, ItemStack gearPiece) {
        super(spatial, Padding.ZERO, ScreenTextures.INSET_BLACK_BACKGROUND);
        int labelX = 9;
        int labelY = 0;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        Set<ResourceLocation> discoveredModelIds = ClientDiscoveredEntriesData.Models.getDiscoveredModels();
        ObservableSupplier<Set<ResourceLocation>> discoveredModelObserverIds = ClientDiscoveredEntriesData.Models.getObserverModels();
        DiscoveredModelSelectElement.DiscoveredModelSelectorModel model = new DiscoveredModelSelectElement.DiscoveredModelSelectorModel(
            ObservableSupplier.of(() -> gearPiece, SideOnlyFixer::stackEqualExact), discoveredModelObserverIds, x -> {});
        List<DiscoveredModelSelectElement.TransmogModelEntry> mEntries = model.getEntries();
        if (mEntries.isEmpty()) {
            String itemName = "Item";
            var regName = gearPiece.getItem().getRegistryName();
            if (regName != null) {
                itemName = regName.getPath().replace("_", " ");
            }
            itemName = WordUtils.capitalize(itemName);

            this.addElement(new LabelElement<>(
                Spatials.positionXY(labelX, 9).width(this.innerWidth() - labelX).height(15),
                new TextComponent("No transmogs for " + itemName).withStyle(ChatFormatting.RED), LabelTextStyle.defaultStyle()));
        }
        for (DiscoveredModelSelectElement.TransmogModelEntry x : mEntries) {
            ItemStack displayStack = new ItemStack(gearPiece.getItem());
            VaultGearData gearData = VaultGearData.read(displayStack);
            gearData.setState(VaultGearState.IDENTIFIED);
            gearData.createOrReplaceAttributeValue(ModGearAttributes.GEAR_MODEL, x.getModelId());
            gearData.write(displayStack);
            this.addElement(new FakeItemSlotElement<>(Spatials.positionXY(labelX, labelY).width(16).height(16), () -> displayStack, () -> false, ScreenTextures.EMPTY, ScreenTextures.EMPTY));
            var oMod = ModDynamicModels.REGISTRIES.getModel(gearPiece.getItem(), x.getModelId());
            if (oMod.isPresent()) {
                var mod = oMod.get();
                VaultGearRarity rollRarity = ModConfigs.GEAR_MODEL_ROLL_RARITIES.getRarityOf(gearPiece, mod.getId());
                this.addElement(new LabelElement<>(
                    Spatials.positionXY(labelX + 20, labelY + 6).width(this.innerWidth() - labelX).height(15),
                    new TextComponent(mod.getDisplayName()).withStyle(Style.EMPTY.withColor(rollRarity.getColor().getValue())), LabelTextStyle.defaultStyle()));

                NineSliceButtonElement<?> btn = new NineSliceButtonElement<>(Spatials.positionXY(0, labelY ).width(innerWidth()).height(18),
                    new NineSliceButtonElement.NineSliceButtonTextures(BUTTON_EMPTY, BUTTON_EMPTY, BUTTON_EMPTY, BUTTON_EMPTY_DISABLED), () -> {});
                boolean canTransmog = TransmogTableBlock.canTransmogModel(player, discoveredModelIds, x.getModelId());
                btn.setDisabled(!canTransmog);
                this.addElement(btn);


            }

            labelY += 18;
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
        return new TransmogListContainer(spatial, gearPiece);
    }

    @Override public Component getTitle() {
        return new TranslatableComponent("vhatcaniroll.screen.title.transmogs").withStyle(ChatFormatting.BLACK);
    }

    @Override public boolean enableCategoryButtons() {
        return false;
    }

    @Override public boolean enableLevelButtons() {
        return false;
    }
}