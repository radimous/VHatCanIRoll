package com.radimous.vhatcaniroll.logic;

import iskallia.vault.core.card.modifier.deck.DeckModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.fml.ModList;
import xyz.iwolfking.woldsvaults.modifiers.deck.ArcaneSlotDeckModifier;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

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
            return null;
        }
    }
}
