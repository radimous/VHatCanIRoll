package com.radimous.vhatcaniroll.ui.greedquests.inner;

import com.radimous.vhatcaniroll.logic.Greed;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.FakeItemSlotElement;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.VerticalScrollClipContainer;
import iskallia.vault.client.gui.framework.render.TooltipDirection;
import iskallia.vault.client.gui.framework.render.Tooltips;
import iskallia.vault.client.gui.framework.spatial.Padding;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.core.DataInitializationItem;
import iskallia.vault.item.crystal.CrystalData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class GreedQuestListContainer extends VerticalScrollClipContainer<GreedQuestListContainer> implements InnerGreedScreen {

    public GreedQuestListContainer(ISpatial spatial) {
        super(spatial, Padding.ZERO, ScreenTextures.INSET_BLACK_BACKGROUND);
        int labelX = 9;
        int labelY = 10;

        List<Component> modifiers = Greed.getQuests();

        if (modifiers.isEmpty()) {
            LabelElement<?> labelelement = new LabelElement<>(
                Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15), new TextComponent(
                "No greed quests found"), LabelTextStyle.defaultStyle()
            );
            this.addElement(labelelement);
            return;
        }
        this.addElement(new LabelElement<>(
            Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15), new TextComponent("Greed Quests"), LabelTextStyle.defaultStyle()
        ));
        labelY += 15;

        for (Component mc : modifiers) {

            if (mc instanceof TextComponent tc){ // try to make wrapped text
                String stripped = tc.getText().stripLeading();
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

        labelY += 10;
        this.addElement(new LabelElement<>(
            Spatials.positionXY(labelX, labelY).width(this.innerWidth() - labelX).height(15), new TextComponent("Greed Challenges"), LabelTextStyle.defaultStyle()
        ));
        labelY += 15;
        modifiers = Greed.getChallenges();
        for (Component mc : modifiers) {

            if (mc instanceof TextComponent tc) {
                if (tc.getText().startsWith("challenge_crystal_id:")) {
                    String crystalId = tc.getText().substring("challenge_crystal_id:".length());
                    var crystal = ModConfigs.CHALLENGE_CRYSTALS.getChallenge(crystalId).orElse(null);
                    if (crystal != null) {
                        ItemStack stack = new ItemStack(ModItems.VAULT_CRYSTAL);
                        stack.getOrCreateTag().putString("ChallengeId", crystalId);
                        DataInitializationItem.doInitialize(stack);
                        CrystalData data = CrystalData.read(stack);
                        data.write(stack);

                        this.addElement(new FakeItemSlotElement<>(
                            Spatials.positionXY(labelX, labelY).width(16).height(16), () -> stack, () -> false, ScreenTextures.EMPTY, ScreenTextures.EMPTY, 16, 16)
                            .tooltip(
                                (tooltipRenderer, poseStack, mouseX, mouseY, tooltipFlag) -> {
                                    tooltipRenderer.renderTooltip(poseStack, stack, mouseX, mouseY,TooltipDirection.RIGHT);
                                    return true;
                                }
                            ));
                        continue;
                    }
                }

                // try to make wrapped text
                String stripped = tc.getText().stripLeading();
                String removed = tc.getText().substring(0, tc.getText().length() - stripped.length());
                int whiteSpaceWidth = Minecraft.getInstance().font.width(removed) + 20; // add extra space for crystal icon
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
    public InnerGreedScreen create(ISpatial spatial) {
        return new GreedQuestListContainer(spatial);
    }
}