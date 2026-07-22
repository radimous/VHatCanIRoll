package com.radimous.vhatcaniroll.ui.cards.inner;

import com.radimous.vhatcaniroll.logic.CardRolls;
import com.radimous.vhatcaniroll.ui.UIUtil;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.FakeItemSlotElement;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.VerticalScrollClipContainer;
import iskallia.vault.client.gui.framework.element.spi.IElement;
import iskallia.vault.client.gui.framework.render.TooltipDirection;
import iskallia.vault.client.gui.framework.spatial.Padding;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.DeckSocketItem;
import iskallia.vault.item.core.DataInitializationItem;
import iskallia.vault.item.crystal.CrystalData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public class DeckCoresListContainer extends VerticalScrollClipContainer<DeckCoresListContainer> implements InnerCardScreen {

    public DeckCoresListContainer(ISpatial spatial) {
        super(spatial, Padding.ZERO, ScreenTextures.INSET_BLACK_BACKGROUND);
        int labelX = 9;
        int labelY = 10;

        List<Component> modifiers = CardRolls.getDeckCoresList();

        if (modifiers.isEmpty()) {
            LabelElement<?> labelelement = new LabelElement<>(
                Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15), new TextComponent(
                "No deck cores found "), LabelTextStyle.defaultStyle()
            );
            this.addElement(labelelement);
            return;
        }

        for (Component mc : modifiers) {
            if (mc instanceof TextComponent tc){ // try to make wrapped text
                String stripped = tc.getText().stripLeading();
                if (stripped.startsWith("Model: ")) {
                    ItemStack stack = new ItemStack(ModItems.DECK_SOCKET);
                    stack.getOrCreateTag().putString("ModifierModel", stripped.replace("Model: ", ""));
//                    DataInitializationItem.doInitialize(stack);
                    this.addElement(new FakeItemSlotElement<>(
                            Spatials.positionXY(5, labelY - 5).width(16).height(16), () -> stack, () -> false, ScreenTextures.EMPTY, ScreenTextures.EMPTY, 16, 16)
                            .tooltip(
                                    (tooltipRenderer, poseStack, mouseX, mouseY, tooltipFlag) -> {
                                        tooltipRenderer.renderTooltip(poseStack, stack, mouseX, mouseY, TooltipDirection.RIGHT);
                                        return true;
                                    }
                            ));
                    continue;
                }
                String removed = tc.getText().substring(0, tc.getText().length() - stripped.length());
                int whiteSpaceWidth = Minecraft.getInstance().font.width(removed);
                var newTc = new TextComponent(stripped).withStyle(tc.getStyle());
                for (var sibling: tc.getSiblings()){
                    if (sibling.getString().equals(tc.getText())) {
                        continue;
                    }
                    newTc.append(sibling);
                }

                LabelElement<?> mcl = new LabelElement<>(
                        Spatials.positionXY(labelX + whiteSpaceWidth , labelY).width(this.innerWidth() - labelX - whiteSpaceWidth),
                        Spatials.width(this.innerWidth() - labelX * 2 - whiteSpaceWidth).height(9),
                        newTc, LabelTextStyle.wrap());
                this.addElement(mcl);
                labelY += Math.max(mcl.getTextStyle().calculateLines(newTc, mcl.width() - whiteSpaceWidth) * 10, 10);
            } else {
                LabelElement<?> labelelement = new LabelElement<>(
                        Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15), mc, LabelTextStyle.defaultStyle()
                );
                this.addElement(labelelement);
                labelY += 10;
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
    public InnerCardScreen create(ISpatial spatial) {
        return new DeckCoresListContainer(spatial);
    }
}