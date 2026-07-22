package com.radimous.vhatcaniroll.logic;

import iskallia.vault.core.card.modifier.deck.DeckModifier;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;
import xyz.iwolfking.woldsvaults.modifiers.deck.AdjacencyBonusDeckModifier;

import javax.annotation.Nullable;
import java.util.List;

public class Wolds {
    public static boolean cardRoll(List<Component> ret, DeckModifier.Config config) {
        if (!ModList.get().isLoaded("woldsvaults")) {
            return false;
        }
        try {
            var cmp = CursedWoldsThings.gerCardComponent(config);
            if (cmp != null) {
                ret.add(cmp);
                return true;
            }
        } catch (Throwable ignored) {} // just in case

        return false;
    }


    // NEVER load this class without checking ModList.get().isLoaded("woldsvaults")
    public static class CursedWoldsThings {
        private static @Nullable Component gerCardComponent(DeckModifier.Config config) {
            if (config instanceof AdjacencyBonusDeckModifier.Config adjacencyConfig) {

            }

            return null;
        }
    }
}
