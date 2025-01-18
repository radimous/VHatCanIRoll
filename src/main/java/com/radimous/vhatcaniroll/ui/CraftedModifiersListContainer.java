package com.radimous.vhatcaniroll.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.radimous.vhatcaniroll.Config;
import com.radimous.vhatcaniroll.logic.ModifierCategory;
import iskallia.vault.client.atlas.TextureAtlasRegion;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.ButtonElement;
import iskallia.vault.client.gui.framework.element.DynamicLabelElement;
import iskallia.vault.client.gui.framework.element.FakeItemSlotElement;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.NineSliceButtonElement;
import iskallia.vault.client.gui.framework.element.VerticalScrollClipContainer;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.screen.layout.ScreenLayout;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.IPosition;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import iskallia.vault.config.gear.VaultGearWorkbenchConfig;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static iskallia.vault.client.gui.framework.ScreenTextures.BUTTON_EMPTY;
import static iskallia.vault.client.gui.framework.ScreenTextures.BUTTON_EMPTY_16_48_TEXTURES;
import static iskallia.vault.client.gui.framework.ScreenTextures.BUTTON_EMPTY_DISABLED;


public class CraftedModifiersListContainer extends VerticalScrollClipContainer<CraftedModifiersListContainer>
    implements InnerGearScreen {

    public CraftedModifiersListContainer(ISpatial spatial, int lvl, ModifierCategory modifierCategory,
                                         ItemStack gearPiece) {
        super(spatial);
        int labelX = 9;
        int labelY = 0;

        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        var stackCopy = gearPiece.copy();
        VaultGearData gearData = VaultGearData.read(stackCopy);
        gearData.setItemLevel(lvl);
        gearData.setState(VaultGearState.IDENTIFIED);
        gearData.createOrReplaceAttributeValue(ModGearAttributes.PREFIXES, 1);
        gearData.createOrReplaceAttributeValue(ModGearAttributes.SUFFIXES, 1);
        gearData.write(stackCopy);



        var crMods = VaultGearWorkbenchConfig.getConfig(gearPiece.getItem())
            .map(VaultGearWorkbenchConfig::getAllCraftableModifiers).orElse(null);
        if (crMods == null) {
            cookieDialog();
            return;
        }

        for (VaultGearWorkbenchConfig.CraftableModifierConfig mod : crMods) {
            boolean disabled = false;

            // is unlocked?
            MutableComponent fullCmp = new TextComponent("");
            var mm = mod.createModifier().orElse(null);
            if (mm == null) {
                continue;
            }
            var oCfgDisplay = mm.getConfigDisplay(gearPiece);
            oCfgDisplay.ifPresent(fullCmp::append);
            MutableComponent restriction = new TextComponent("");
            int minLevel = mod.getMinLevel();
            if (mod.getUnlockCategory() == VaultGearWorkbenchConfig.UnlockCategory.LEVEL && lvl < minLevel) {
                if (!Config.SHOW_UNOBTAINABLE_CRAFTED.get()){
                    continue;
                }
                restriction.append(new TextComponent(" LVL " + minLevel + "+").withStyle(ChatFormatting.DARK_RED));
                disabled = true;
            }
            if (mod.getUnlockCategory() == VaultGearWorkbenchConfig.UnlockCategory.VAULT_DISCOVERY && !mod.hasPrerequisites(player)) {
                if (minLevel > lvl) {
                    if (!Config.SHOW_UNOBTAINABLE_CRAFTED.get()){
                        continue;
                    }
                    restriction.append(new TextComponent(" ARCHIVE").withStyle(ChatFormatting.DARK_RED));
                } else {
                    restriction.append(new TextComponent(" ARCHIVE").withStyle(Style.EMPTY.withColor(TextColor.parseColor("#ff6f00"))));
                }
                disabled = true;
            }
            LabelElement<?> labelelement = new LabelElement<>(
                Spatials.positionXY(8, labelY + 5).width(this.innerHeight() - labelX), fullCmp,
                LabelTextStyle.defaultStyle().shadow() // WHY DOESN'T SHADOW WORK?
            );
            NineSliceButtonElement<?> btn = new NineSliceButtonElement<>(Spatials.positionXY(0, labelY ).width(innerWidth()).height(18),
                new NineSliceButtonElement.NineSliceButtonTextures(BUTTON_EMPTY, BUTTON_EMPTY, BUTTON_EMPTY, BUTTON_EMPTY_DISABLED), () -> {});


            //align right
            LabelElement<?> restrictionLabel = new LabelElement<>(
                Spatials.positionXY(btn.right() - 70, labelY + 5).width(this.innerHeight() - labelX).height(14), restriction,
                LabelTextStyle.defaultStyle().border4()
            );
            btn.setDisabled(disabled);
            this.addElement(btn);
            this.addElement(labelelement);
            this.addElement(restrictionLabel);
            labelY += 18;

        }


    }

    public float getScroll() {
        return 0;
    }

    public void setScroll(float scroll) {
    }

    @Override
    public InnerGearScreen create(ISpatial spatial, int lvl, ModifierCategory modifierCategory, ItemStack gearPiece) {
        return new CraftedModifiersListContainer(spatial, lvl, modifierCategory, gearPiece);
    }




    // :)
    private void cookieDialog(){
        var noModifiers = new LabelElement<>(Spatials.positionXY(innerWidth()/2 - 60, 20).width(18).height(18), new TextComponent("No crafted modifiers found."), LabelTextStyle.defaultStyle().border4().center());
        var wantCookie = new LabelElement<>(Spatials.positionXY(innerWidth()/2 - 50, 80).width(18).height(18), new TextComponent("Would you like a cookie?"), LabelTextStyle.defaultStyle().border4().center());
        var yesBtn = new ButtonElement<>(Spatials.positionXY(innerWidth()/2 - 20, 100).width(100).height(20), BUTTON_EMPTY_16_48_TEXTURES, () -> {});
        var noBtn = new ButtonElement<>(Spatials.positionXY(innerWidth()/2 - 20, 120).width(100).height(20), BUTTON_EMPTY_16_48_TEXTURES, () -> { /* sad cookie monster noises */});
        var yesLabel = new LabelElement<>(Spatials.positionXY(innerWidth()/2 - 6, 105).width(18).height(18), new TextComponent("Yes"), LabelTextStyle.defaultStyle().border4().center());
        var noLabel = new LabelElement<>(Spatials.positionXY(innerWidth()/2 - 3, 125).width(18).height(18), new TextComponent("No"), LabelTextStyle.defaultStyle().border4().center());
        var sadCookie = new LabelElement<>(Spatials.positionXY(innerWidth()/2 - 65, 145).width(18).height(18), new TextComponent("*Sad cookie monster noises*").withStyle(ChatFormatting.GRAY), LabelTextStyle.defaultStyle().center());
        yesBtn.setOnClick(() -> {
            this.removeElement(noModifiers);
            this.removeElement(wantCookie);
            this.removeElement(yesBtn);
            this.removeElement(noBtn);
            this.removeElement(yesLabel);
            this.removeElement(noLabel);
            this.removeElement(sadCookie);
            ScreenLayout.requestLayout();
            cookie();
        });

        noBtn.setOnClick(() -> {
            this.addElement(sadCookie);
            noBtn.setDisabled(true);
            ScreenLayout.requestLayout();
        });
        this.addElement(noModifiers);
        this.addElement(wantCookie);
        this.addElement(yesLabel);
        this.addElement(noLabel);
        this.addElement(noBtn);
        this.addElement(yesBtn);


    }
    private void cookie(){
        AtomicInteger counter = new AtomicInteger(0);
            var slot = new ScaledFakeItemSlotElement<>(Spatials.positionXY(innerWidth()/2, innerHeight()/2), () -> new ItemStack(Items.COOKIE), () -> false, ScreenTextures.EMPTY, ScreenTextures.EMPTY,8*16,8*16, 8f);
        slot.whenClicked(counter::getAndIncrement);
        this.addElement(slot);
        this.addElement(new CookieLabelElement(Spatials.positionXY(innerWidth()/2, innerHeight()/2 - 70).width(18).height(18), counter::get, LabelTextStyle.defaultStyle().border4().center()));
    }

    private class CookieLabelElement extends DynamicLabelElement<Integer, CookieLabelElement>{
        public CookieLabelElement(IPosition position,
                                  Supplier<Integer> valueSupplier, LabelTextStyle.Builder labelTextStyle) {
            super(position, valueSupplier, labelTextStyle);
        }

        @Override protected void onValueChanged(Integer count) {
            this.set(new TextComponent(count.toString()));
        }

        @Override public void render(IElementRenderer renderer, @NotNull PoseStack poseStack, int mouseX, int mouseY,
                                     float partialTick) {
            if (this.valueSupplier.get() == 0) {
                return;
            }
            var ll = RenderSystem.getModelViewStack();
            ll.pushPose();
            float scale = 2f;
            ll.scale(scale, scale, scale);
            ll.translate(-getWorldSpatial().x() * (1 - 1/scale), -getWorldSpatial().y() * (1 - 1/scale), 0);
            RenderSystem.applyModelViewMatrix();

            super.render(renderer, poseStack, mouseX, mouseY, partialTick);
            ll.popPose();

        }
    }

    private static class ScaledFakeItemSlotElement <E extends ScaledFakeItemSlotElement<E>> extends FakeItemSlotElement<E> {
        private float scale;
        private long lastClicked = 0;
        public ScaledFakeItemSlotElement(ISpatial spatial, Supplier<ItemStack> itemStack, Supplier<Boolean> disabled, TextureAtlasRegion slotTexture, TextureAtlasRegion disabledSlotTexture, int width, int height, float scale) {
            super(Spatials.positionX((int) (spatial.x() - (scale * 9))).positionY((int) (spatial.y() - (scale * 9))), itemStack, disabled, slotTexture, disabledSlotTexture, width, height);
            this.scale = scale;
        }

        @Override public void render(IElementRenderer renderer, @NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            float originalScale = scale;
            long msDiff = System.currentTimeMillis() - lastClicked;
            if (msDiff < 150) {
                scale *= 1 + msDiff * 0.001f;
            } else if (msDiff < 300) {
                scale *= 1 + (300 - msDiff) * 0.001f;
            }
            var viewStack = RenderSystem.getModelViewStack();
            viewStack.pushPose();
            viewStack.translate(- (scale - originalScale) * originalScale, - (scale - originalScale) * originalScale, 0);
            viewStack.scale(scale, scale, 1);
            viewStack.translate(-getWorldSpatial().x() * (1 - 1/scale), -getWorldSpatial().y() * (1 - 1/scale), 0);
            RenderSystem.applyModelViewMatrix();
            super.render(renderer, poseStack, mouseX, mouseY, partialTick);
            viewStack.popPose();
            scale = originalScale;
        }

        @Override public boolean mouseClicked(double mouseX, double mouseY, int buttonIndex) {
            if (this.isEnabled() && this.containsMouse(mouseX, mouseY) && buttonIndex == 0)
                lastClicked = System.currentTimeMillis();
            return super.mouseClicked(mouseX, mouseY, buttonIndex);
        }
    }
}