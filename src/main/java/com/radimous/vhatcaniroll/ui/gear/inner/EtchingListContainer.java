package com.radimous.vhatcaniroll.ui.gear.inner;

import com.radimous.vhatcaniroll.logic.Etchings;
import com.radimous.vhatcaniroll.logic.ModifierCategory;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.FakeItemSlotElement;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.NineSliceElement;
import iskallia.vault.client.gui.framework.element.VerticalScrollClipContainer;
import iskallia.vault.client.gui.framework.spatial.Padding;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import iskallia.vault.config.gear.VaultEtchingConfig;
import iskallia.vault.gear.VaultGearType;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.gear.EtchingItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.text.WordUtils;

import java.util.*;

import static com.radimous.vhatcaniroll.VHatCanIRoll.ERROR_STYLE;

public class EtchingListContainer extends VerticalScrollClipContainer<EtchingListContainer> implements InnerGearScreen {

    private static final Comparator<VaultEtchingConfig.EtchingEntry> ETCHING_COMPARATOR = Comparator.comparingInt(VaultEtchingConfig.EtchingEntry::getMinGreedTier).thenComparing(VaultEtchingConfig.EtchingEntry::getName);
    Map<ResourceLocation, Integer> scrollPositions = new HashMap<>();
    int maxHeight = 1;
    public EtchingListContainer(ISpatial spatial, ItemStack gearPiece) {
        super(spatial, Padding.ZERO, ScreenTextures.INSET_BLACK_BACKGROUND);
        int labelX = 9;
        int labelY = 7;

        var etchingValues = ModConfigs.ETCHINGS.getEtchingConfigs();
        VaultGearType type = gearPiece.getItem() instanceof VaultGearItem gear ? gear.getGearType(gearPiece) : null;
        if (type == null) {
            return;
        }

        var goodEntries = etchingValues.stream().filter(entry ->
            entry.getTypeGroups().stream().anyMatch(
                g ->  ModConfigs.ETCHINGS.getGroup(g).contains(type)
            )
        ).sorted(ETCHING_COMPARATOR).toList();

        for (VaultEtchingConfig.EtchingEntry entry : goodEntries) {
            var id = getId(entry);
            if (id == null) {
                labelY += 5;
                LabelElement<?> nameLabel = new LabelElement<>(
                    Spatials.positionXY(labelX + 20, labelY).width(this.innerWidth() - labelX).height(15),
                    Spatials.positionXY(labelX + 20, labelY).width(this.innerWidth() - labelX).height(15),
                    new TextComponent("ERR - etching ID is null").withStyle(ERROR_STYLE), LabelTextStyle.defaultStyle()
                );
                this.addElement(nameLabel);
                labelY += 20;
                continue;
            }

            int iconHeight = labelY;

            labelY += 5;
            LabelElement<?> nameLabel = new LabelElement<>(
                Spatials.positionXY(labelX + 20, labelY).width(this.innerWidth() - labelX).height(15),
                Spatials.positionXY(labelX + 20, labelY).width(this.innerWidth() - labelX).height(15),
                new TextComponent(entry.getName()).withStyle(Style.EMPTY.withColor(entry.getComponentColor())).append(new TextComponent("  Greed Tier "+entry.getMinGreedTier() + "+").withStyle(ChatFormatting.DARK_GRAY)), LabelTextStyle.defaultStyle()
            );
            this.addElement(nameLabel);
            labelY += 20;

            ItemStack displayStack = EtchingItem.create(id, entry, new Random(), entry.getMinGreedTier()).orElse(new ItemStack(Items.BARRIER));
            this.addElement(new FakeItemSlotElement<>(Spatials.positionXY(labelX - 4, iconHeight).width(16).height(16), () -> displayStack, () -> false, ScreenTextures.EMPTY, ScreenTextures.EMPTY));
            scrollPositions.put(id, iconHeight);

            List<Component> mlist = Etchings.getEtchingInfo(entry);
            for (Component mc : mlist) {
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
            }
            this.addElement(new NineSliceElement<>(
                Spatials.positionXY(0, labelY).width(this.innerWidth()).height(3),
                ScreenTextures.BUTTON_EMPTY));
            labelY += 10;
            maxHeight = labelY;
        }
        if (goodEntries.isEmpty()) {
            String itemName = "Item";
            var regName = gearPiece.getItem().getRegistryName();
            if (regName != null) {
                itemName = regName.getPath().replace("_", " ");
            }
            itemName = WordUtils.capitalize(itemName);

            this.addElement(new LabelElement<>(
                Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15),
                new TextComponent("No etching for " + itemName).withStyle(ERROR_STYLE), LabelTextStyle.defaultStyle()));
            labelY += 10;
        }
    }
    private ResourceLocation getId(VaultEtchingConfig.EtchingEntry etching) {
        var ids = ModConfigs.ETCHINGS.getEtchingIds();
        for (var id: ids) {
            var cfg = ModConfigs.ETCHINGS.getEtchingConfig(id);
            if (cfg == etching) {
                return id;
            }
        }
        return null;
    }

    public float getScroll() {
        return this.verticalScrollBarElement.getValue();
    }

    public void setScroll(float scroll) {
        this.verticalScrollBarElement.setValue(scroll);
    }

    @Override
    public InnerGearScreen create(ISpatial spatial, int lvl, ModifierCategory modifierCategory, ItemStack gearPiece, boolean mythic) {
        return new EtchingListContainer(spatial, gearPiece);
    }

    @Override public Component getTitle() {
        return new TranslatableComponent("vhatcaniroll.screen.title.etchings").withStyle(ChatFormatting.BLACK);
    }

    @Override public boolean enableCategoryButtons() {
        return false;
    }

    @Override public boolean enableLevelButtons() {
        return false;
    }

    public void scrollToEtching(ResourceLocation id) {
        Integer pos = scrollPositions.get(id);
        if (pos == null) {
            return;
        }
        int scrollPos = Math.max(0, pos - 3);
        int maxScroll = Math.max(1, maxHeight - innerHeight());

        float scrollRatio = (float) scrollPos / maxScroll;
        setScroll(Mth.clamp(scrollRatio, 0, 1));
    }

}