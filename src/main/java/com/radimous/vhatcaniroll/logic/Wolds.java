package com.radimous.vhatcaniroll.logic;

import iskallia.vault.core.card.modifier.deck.DeckModifier;
import iskallia.vault.util.StringUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.fml.ModList;
import xyz.iwolfking.woldsvaults.modifiers.deck.ArcaneLevelDeckModifier;
import xyz.iwolfking.woldsvaults.modifiers.deck.ArcaneSlotDeckModifier;
import xyz.iwolfking.woldsvaults.modifiers.deck.CardNeighborTypeDeckModifier;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.radimous.vhatcaniroll.logic.CardRolls.*;

public class Wolds {
    public static boolean cardRoll(List<Component> ret, DeckModifier<?> modifier, DeckModifier.Config config, Component ttipComponent) {
        if (!ModList.get().isLoaded("woldsvaults")) {
            return false;
        }
        try {
            var cmp = CursedWoldsThings.getCardComponents(config, modifier, ttipComponent);
            if (cmp != null) {
                ret.addAll(cmp);
                return true;
            }
        } catch (Throwable ignored) {} // just in case

        return false;
    }


    // NEVER load this class without checking ModList.get().isLoaded("woldsvaults")
    public static class CursedWoldsThings {
        private static @Nullable List<Component> getCardComponents(DeckModifier.Config config, DeckModifier<?> modifier, Component ttipComponent) {
            if (modifier instanceof ArcaneSlotDeckModifier && config instanceof ArcaneSlotDeckModifier.Config arcaneConfig) {
                List<Component> ret = new ArrayList<>();
                boolean moreThanOne = arcaneConfig.slotRolls.containsKey("lesser") || arcaneConfig.slotRolls.containsKey("greater");
                if (arcaneConfig.slotRolls.containsKey("lesser")) {
                    String slotRollL = processIntroll(arcaneConfig.slotRolls.get("lesser").getSlotRoll(arcaneConfig.slotRoll));
                    String levelsL = processFloatroll(arcaneConfig.modifierRolls.get("lesser").getRoll(arcaneConfig.modifierRoll));
                    ret.add(new TextComponent( "     Lesser: " + slotRollL + " Arcane slots gains " + levelsL + " additional levels"));
                }
                String slotRoll = processIntroll(arcaneConfig.slotRoll);
                String levels = processFloatroll(arcaneConfig.modifierRoll);
                ret.add(new TextComponent( (moreThanOne ? "     Normal: "  : "     ") + slotRoll + " Arcane slots gains " + levels + " additional levels"));

                if (arcaneConfig.slotRolls.containsKey("greater")) {
                    String slotRollG = processIntroll(arcaneConfig.slotRolls.get("greater").getSlotRoll(arcaneConfig.slotRoll));
                    String levelsG = processFloatroll(arcaneConfig.modifierRolls.get("greater").getRoll(arcaneConfig.modifierRoll));
                    ret.add(new TextComponent( "     Greater: " + slotRollG + " Arcane slots gains " + levelsG + " additional levels"));
                }
                return ret;
            }

            if (modifier instanceof CardNeighborTypeDeckModifier && config instanceof CardNeighborTypeDeckModifier.Config neighborConfig) {
                List<Component> ret = new ArrayList<>();

                String neighborTypesString = neighborConfig.getValidNeighorTypes().stream().map(Enum::name).map(StringUtils::convertToTitleCase).collect(Collectors.joining(", "));
                String additionalTimes = processFloatroll(neighborConfig.modifierRoll, - 1);
                MutableComponent comp = (new TextComponent("     Cards that scale with ")).withStyle(ChatFormatting.GRAY)
                        .append((new TextComponent("[" + neighborTypesString + "]")).withStyle(ChatFormatting.GOLD))
                        .append((new TextComponent(" count cards an additional ")).withStyle(ChatFormatting.GRAY))
                        .append((new TextComponent(additionalTimes)).withStyle(ChatFormatting.WHITE))
                        .append((new TextComponent(" time(s).")).withStyle(ChatFormatting.GRAY));
                ret.add(comp);
                return ret;
            }

            if (modifier instanceof ArcaneLevelDeckModifier) {
                List<Component> ret = new ArrayList<>();
                var comp = new TextComponent("     Arcane cards have +" + processFloatroll(config.modifierRoll) + " ability level(s).");
                ret.add(comp);
                return ret;
            }
            return null;
        }
    }
}
