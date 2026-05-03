package com.radimous.vhatcaniroll.logic;

import iskallia.vault.config.greed.GreedChallengeEntry;
import iskallia.vault.config.greed.GreedQuestEntry;
import iskallia.vault.core.world.roll.IntRoll;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.core.DataInitializationItem;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.task.ProgressConfiguredTask;
import iskallia.vault.task.Task;
import iskallia.vault.task.counter.TargetTaskCounter;
import iskallia.vault.task.counter.TaskCounter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Greed {
    public static List<Component> getQuests() {
        List<Component> ret = new ArrayList<>();
        List<GreedQuestEntry> quests = ModConfigs.GREED_TRADER.getQuests();
        for (var quest : quests) {

            var configTask = quest.getTask();
            TaskCounter.Config cfg = null;

            for (Task task : configTask.getSelfAndDescendants()) {
                if (task instanceof ProgressConfiguredTask progressTask
                    && progressTask.getCounter() instanceof TargetTaskCounter targetCounter) {
                    if (targetCounter.getConfig() instanceof TargetTaskCounter.Config targetConfig) {
                        cfg = targetConfig;
                        break;
                    }
                }
            }


            String target = "";
            String scalar = " +"+(Math.round(100 * (quest.getScalar()-1))) + "%";
            if (cfg instanceof TargetTaskCounter.Config targetTaskCounter)  {
                if (targetTaskCounter.getTarget() instanceof IntRoll.Uniform uniform) {
                    target = uniform.getMin() + "-" + uniform.getMax();
                }
                if (targetTaskCounter.getTarget() instanceof IntRoll.Constant constant) {
                    if (constant.getMin() == constant.getMax()) {
                        target = String.valueOf(Math.max(1, constant.getMin()));
                    } else {
                        target = constant.getMin() + "-" + constant.getMax();
                    }
                }
            }

            String repRange =  quest.getRepRewardMin() + "-" + quest.getRepRewardMax();
            if (repRange.length() == 4) {
                repRange = "  " + repRange;
            }
            var split = quest.getDescription().split("%target");
            String fst = split[0];
            MutableComponent descComp = new TextComponent(split[0]).withStyle(ChatFormatting.WHITE);
            if (split.length > 1) {
                descComp.append(new TextComponent(target).withStyle(ChatFormatting.YELLOW)).append(new TextComponent(scalar).withStyle(ChatFormatting.AQUA)).append(new TextComponent(split[1]).withStyle(ChatFormatting.WHITE));
            }
            ret.add(new TextComponent(repRange).withStyle(ChatFormatting.GOLD).append("  ").append(descComp));

        }


        return ret;
    }

    public static List<Component> getChallenges() {
        List<Component> ret = new ArrayList<>();
        List<GreedChallengeEntry> challenges = ModConfigs.GREED_TRADER.getChallenges();
        for (var challenge : challenges) {

            String greedTier;
            if (challenge.getMinTier() == challenge.getMaxTier()) {
                greedTier = String.valueOf(challenge.getMinTier());
            } else if (challenge.getMaxTier() == -1) {
                greedTier = challenge.getMinTier() + "+";
            } else {
                greedTier = challenge.getMinTier() + "-" + challenge.getMaxTier();
            }
            ret.add(new TextComponent("challenge_crystal_id:" + challenge.getChallengeCrystalId()).withStyle(ChatFormatting.DARK_GRAY));
            ret.add(new TextComponent(challenge.getDisplayName()).append(new TextComponent(" Greed Tier " + greedTier).withStyle(ChatFormatting.DARK_GRAY)).withStyle(ChatFormatting.GOLD));
            ret.add(new TextComponent(challenge.getDescription()));
            ret.add(new TextComponent(""));
        }


        return ret;
    }
}