package com.radimous.vhatcaniroll.logic;

import com.radimous.vhatcaniroll.Config;
import com.radimous.vhatcaniroll.mixin.accessors.cards.*;
import iskallia.vault.config.card.BoosterPackConfig;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.core.card.CardCondition;
import iskallia.vault.core.card.CardEntry;
import iskallia.vault.core.card.CardProperty;
import iskallia.vault.core.card.CardScaler;
import iskallia.vault.core.card.modifier.card.CardModifier;
import iskallia.vault.core.card.modifier.card.DummyCardModifier;
import iskallia.vault.core.card.modifier.card.GearCardModifier;
import iskallia.vault.core.card.modifier.card.TaskLootCardModifier;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.world.loot.entry.ItemLootEntry;
import iskallia.vault.core.world.roll.IntRoll;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.task.ProgressConfiguredTask;
import iskallia.vault.task.Task;
import iskallia.vault.task.counter.TargetTaskCounter;
import iskallia.vault.task.renderer.CardTaskRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;

public class CardRolls {
    public static List<Component> getAll() {
        List<Component> ret = new ArrayList<>();

        ret.addAll(getBoosterPackList());
        for (int i = 0 ; i< 2 ; i++) {
            ret.add(new TextComponent(""));
        }

        ret.addAll(getModifierList());

        for (int i = 0 ; i< 2 ; i++) {
            ret.add(new TextComponent(""));
        }

        ret.addAll(getConditionsList());

        for (int i = 0 ; i< 2 ; i++) {
            ret.add(new TextComponent(""));
        }

        ret.addAll(getScalerList());

        for (int i = 0 ; i< 2 ; i++) {
            ret.add(new TextComponent(""));
        }

        ret.addAll(getTaskList());


        System.out.println(ret.size());
        return ret;
    }



    public static List<Component> getBoosterPackList() {
        List<Component> ret = new ArrayList<>();
        ret.add(new TextComponent("BOOSTER PACKS").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.AQUA));
        ret.add(new TextComponent(""));
        var boosterPack = ModConfigs.BOOSTER_PACK;
        var packVal = boosterPack.getValues();
        for (var bp : packVal.entrySet()) {
            var kk = bp.getKey();
            var vv = bp.getValue();
            var vvA = (BoosterPackEntryAccessor) vv;

            ret.add(new TextComponent("").append(vvA.getName()));
            if (Config.DEBUG_CARDS.get()) {
                ret.add(new TextComponent("  ID: " + kk));
                ret.add(new TextComponent("  MODEL: ").append(vvA.getModel().getUnopened()));
            }
            ret.add(new TextComponent("  CARDS: ").append(formatInlineWeightedList(vvA.getRoll())));
            ret.add(new TextComponent("  CARD COLOR: ").append(formatInlineWeightedList(vvA.getColor())));
            ret.add(new TextComponent("  CARD TIER: ").append(formatInlineWeightedList(vvA.getTier())));


            for (var pool : vv.getCard().entrySet()) {
                var poolKey = pool.getKey(); // "default"
                var poolWeight = pool.getValue();
                ret.add(new TextComponent("    WEIGHT: "+poolWeight).withStyle(ChatFormatting.GRAY));
                for (BoosterPackConfig.CardConfig cardConfig: poolKey) {
                    var modifier = cardConfig.getModifier();
                    var cardConfigA = (BoosterPackConfigCardConfigAccessor) cardConfig;
                    WeightedList<List<CardEntry.Color>> cardColor = cardConfigA.getColors();
                    String cardCondition = cardConfigA.getCondition();
                    Set<String> cardGroups = cardConfigA.getGroups();
                    String cardScaler = cardConfigA.getScaler();
                    double chance = cardConfigA.getProbability();

                    boolean debug = Config.DEBUG_CARDS.get();
                    ret.add(new TextComponent("    MODIFIER: ").append(createComponent(modifier)));
                    if (cardColor != null || debug) {
                        ret.add(new TextComponent("    COLOR: ").append(formatInlineWeightedList(cardColor)));
                    }
                    if (cardGroups != null || debug) {
                        ret.add(new TextComponent("    GROUPS: "+ cardGroups));
                    }
                    if ((cardCondition != null && !cardCondition.isEmpty()) || debug) {
                        ret.add(new TextComponent("    CONDITION: ").append(createComponent(cardCondition)));
                    }
                    if ((cardScaler != null && !cardScaler.isEmpty())|| debug) {
                        ret.add(new TextComponent("    SCALER: ").append(createComponent(cardScaler)));
                    }
                    if (chance != 1) {
                        ret.add(new TextComponent("    CHANCE: "+ chance));
                    }
                }
                ret.add(new TextComponent(""));
            }
        }

        return ret;
    }

    public static List<Component> getModifierList() {
        List<Component> ret = new ArrayList<>();
        ret.add(new TextComponent("MODIFIERS").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.AQUA));
        ret.add(new TextComponent(""));

        var modifiers = ModConfigs.CARD_MODIFIERS; // THIS IS CORE

        var modPools = modifiers.getPools();
        var modVals = modifiers.getValues();
        for (var pool : modPools.entrySet()) {
            var poolKey = pool.getKey(); // "default"
            ret.add(new TextComponent(poolKey).withStyle(ChatFormatting.GREEN));
            var poolVal = pool.getValue();
            boolean showWeight = Config.SHOW_WEIGHT.get() && !equalWeight(poolVal);
            for (var cardRoll: poolVal.entrySet()) {
                var cardId = cardRoll.getKey(); // cooldown_reduction
                var cardWeight = cardRoll.getValue();
                var cardConfig = modVals.get(cardId);
                if (Config.DEBUG_CARDS.get()) {
                    var rollCmp = new TextComponent("  id: "+ cardId);
                    if (showWeight) {
                        rollCmp.append(new TextComponent("  WEIGHT: " + cardWeight).withStyle(ChatFormatting.GRAY));
                    }
                    ret.add(rollCmp);
                }

                var cardColor = cardConfig.colors;
                var cardCondition = cardConfig.condition;
                var cardModel = cardConfig.model;
                var cardGroups = cardConfig.groups;
                var cardScaler = cardConfig.scaler;
                var cardValue = cardConfig.value;

                var debug = Config.DEBUG_CARDS.get();
                if (debug) {
                    ret.add(new TextComponent("  MODEL: "+ cardModel));
                }
                if ((cardColor != null && !cardColor.isEmpty()) || debug) {
                    ret.add(new TextComponent("  COLOR: "+ cardColor));
                }
                if (cardGroups !=null || debug) {
                    ret.add(new TextComponent("  GROUPS: "+ cardGroups));
                }
                if (cardCondition != null || debug) {
                    ret.add(new TextComponent("  COND: "+ cardCondition));
                }
                if (cardScaler !=null || debug) {
                    ret.add(new TextComponent("  SCALER: "+ cardScaler));
                }
                if (cardValue instanceof GearCardModifier<?> gearCardModifier) {
                    CardProperty.Config gearCardConfig = gearCardModifier.getConfig();
                    if (gearCardConfig instanceof CardModifier.Config modifierConfig) {
                        int maxTier = modifierConfig.maxTier;
                        if (debug) {
                            ret.add(new TextComponent("  MAX TIER: "+ maxTier));
                        }
                    }
                    if (gearCardConfig instanceof GearCardModifier.Config<?> gearModifierConfig) {
                        VaultGearAttribute<?> atr = gearModifierConfig.getAttribute();
                        Map<Integer, String> configPool = gearModifierConfig.getPool();
                        for (Map.Entry<Integer, String> configPoolVal: configPool.entrySet()) {
                            var singleTierArray = new ArrayList<VaultGearTierConfig.ModifierTier<?>>();
                            singleTierArray.add(new VaultGearTierConfig.ModifierTier<>(0,0, gearModifierConfig.getConfig(configPoolVal.getKey())));
                            var modComp = Modifiers.getModifierComponent(atr, singleTierArray);
                            ret.add(new TextComponent("  T" + configPoolVal.getKey() + ": ").append(modComp));
                        }

                    }
                } else if (cardValue instanceof TaskLootCardModifier taskLootCardModifier) {
                    TaskLootCardModifier.Config taskLootConfig = taskLootCardModifier.getConfig();
                    var taskLootConfigA = (TaskLootCardModifierConfigAccessor)taskLootConfig;
                    if (debug) {
                        ret.add(new TextComponent("    MAX TIER: " + taskLootConfig.maxTier));
                    }
                    ret.add(new TextComponent("    TASK: ").append(createComponent(taskLootConfigA.getTask())));
                    var lootItems = taskLootConfigA.getLoot().getChildren();
                    if (lootItems.size() == 1) {
                        var item = lootItems.entrySet().stream().findFirst().orElse(null);
                        if (item != null) {
                            if (item.getKey() instanceof ItemLootEntry itemLootEntry) {
                                ret.add(new TextComponent("    LOOT: " + processIntroll(itemLootEntry.getCount()) + " " +itemLootEntry.getItem()));
                            } else {
                                ret.add(new TextComponent("    LOOT: UNSUPPORTED (non Item loot)").withStyle(ChatFormatting.RED));
                            }
                        } else {
                            ret.add(new TextComponent("    LOOT: UNSUPPORTED (single NULL)").withStyle(ChatFormatting.RED));
                        }
                    } else {
                        ret.add(new TextComponent("    LOOT: UNSUPPORTED (not a single item)").withStyle(ChatFormatting.RED));
                    }
                    if (taskLootConfigA.getCount().size() == 1) {
                        var cnt = taskLootConfigA.getCount().entrySet().stream().findFirst().orElse(null);
                        if (cnt != null) {
                            ret.add(new TextComponent("    COUNT: " + processIntroll(cnt.getValue())));
                        } else {
                            ret.add(new TextComponent("    COUNT: UNSUPPORTED (null cnt)").withStyle(ChatFormatting.RED));
                        }
                    } else {
                        ret.add(new TextComponent("    COUNT:"));
                        for (var cnt : taskLootConfigA.getCount().entrySet()) {
                            ret.add(new TextComponent("                 - "+cnt.getKey()+ " => " + processIntroll(cnt.getValue())));
                        }
                    }

//                    ret.add(new TextComponent("    HIGHLIGHT: " + taskLootConfigA.getHighlightColor()));
                    ret.add(new TextComponent("    TOOLTIP: ").append(taskLootConfigA.getTooltip()));
                } else if (cardValue instanceof DummyCardModifier) {
                    // WHY IS THIS EVEN A THING
                } else {
                    ret.add(new TextComponent("    UNKNOWN CARD VALUE: "+ cardValue).withStyle(ChatFormatting.RED));
                }
                ret.add(new TextComponent(""));
            }
            ret.add(new TextComponent(""));
        }
        return ret;
    }

    public static List<Component> getConditionsList() {
        List<Component> ret = new ArrayList<>();

        ret.add(new TextComponent("CONDITIONS").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.AQUA));
        ret.add(new TextComponent(""));

        var conditions = (CardConditionsConfigAccessor)ModConfigs.CARD_CONDITIONS;
        Map<String, WeightedList<String>> conditionPools = conditions.getPools();
        Map<String, CardCondition> conditionValues = conditions.getValues();

        for (var pool: conditionPools.entrySet()) {
            var poolKey = pool.getKey();
            var poolValues = pool.getValue();
            ret.add(new TextComponent(poolKey));
            for (var poolValue: poolValues.entrySet()) {
                var condition = conditionValues.get(poolValue.getKey());
//                Map<Integer, List<CardCondition.Filter>> condFilters = condition.getFilters();
//                if (condFilters.isEmpty()) {
//                    continue;
//                }
                CardCondition.Config conditionConfig = condition.getConfig();
                var conditionConfigA = (CardConditionConfigAccessor) conditionConfig;
                var conditionTiers = conditionConfigA.getTiers();
                ret.add(new TextComponent("  " + poolValue.getKey()));
                if (!equalWeight(poolValues)) {
                    ret.add(new TextComponent("    WEIGHT: " + poolValue.getValue()).withStyle(ChatFormatting.GRAY));
                }
                for (var conditionTier : conditionTiers.entrySet()) {
                    if (conditionTiers.size() > 1) {
                        ret.add(new TextComponent("     TIER: "+conditionTier.getKey()));
                    }
                    for (Map.Entry<List<CardCondition.Filter.Config>, Double> kk:  conditionTier.getValue().entrySet()) {
                        var condTierList = kk.getKey();
                        var condTierWeight = kk.getValue();
                        if (!equalWeight(conditionTier.getValue())) {
                            ret.add(new TextComponent("         WEIGHT: " + condTierWeight).withStyle(ChatFormatting.GRAY));
                        }
                        boolean debug = Config.DEBUG_CARDS.get();
                        for (CardCondition.Filter.Config condTier: condTierList) {
                            var condT = (CardConditionFilterConfigAccessor)condTier;
                            if (debug) {
                                ret.add(new TextComponent("         COLOR: ").append(formatInlineWeightedList(condT.getColorFilter())));
                                ret.add(new TextComponent("         GROUP: ").append(formatInlineWeightedList(condT.getGroupFilter())));
                                ret.add(new TextComponent("         NEIGHBOR: ").append(formatInlineWeightedList(condT.getNeighborFilter())));
                                ret.add(new TextComponent("         TIER: ").append(formatInlineWeightedList(condT.getTierFilter())));
                                ret.add(new TextComponent("         MIN COUNT: " + processIntroll(condT.getMinCount())));
                                ret.add(new TextComponent("         MAX COUNT: " + processIntroll(condT.getMaxCount())));
                            }
                            MutableComponent cnt = conditionText(condT);
                            if (cnt != null) {
                                ret.add(new TextComponent("     ").append(cnt));
                            }
                        }
                        ret.add(new TextComponent(""));
                    }
                }
            }
        }

        return ret;
    }

    public static List<Component> getScalerList() {
        List<Component> ret = new ArrayList<>();

        ret.add(new TextComponent("SCALERS").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.AQUA));
        ret.add(new TextComponent(""));

        var scalers = (CardScalersConfigAccessor)ModConfigs.CARD_SCALERS;
        Map<String, WeightedList<String>> scalerPools = scalers.getPools();
        var scalerValues = scalers.getValues();
        for (var pool: scalerPools.entrySet()) {
            var poolKey = pool.getKey();
            var poolValues = pool.getValue();
            ret.add(new TextComponent("POOL: " + poolKey));
            for (var poolValue: poolValues.entrySet()) {
                if (poolValues.size() > 1) {
                    var pvCmp = new TextComponent("  ENTRY: " + poolValue.getKey());
                    if (!equalWeight(poolValues)) {
                        pvCmp.append((new TextComponent("  WEIGHT: " + poolValue.getValue()).withStyle(ChatFormatting.GRAY)));
                    }
                    ret.add(pvCmp);
                }
                CardScaler scalerVal = scalerValues.get(poolValue.getKey());
                CardScaler.Config scalerConfig = scalerVal.getConfig();
                var scalerConfigA = (CardScalerConfigAccessor) scalerConfig;
                for (Map.Entry<Integer, WeightedList<List<CardScaler.Filter.Config>>> conditionTier : scalerConfigA.getTiers().entrySet()) {
                    String indent = poolValues.size() > 1 ? "    " : "  ";
                    if (scalerConfigA.getTiers().size() > 1) {
                        ret.add(new TextComponent("    TIER: "+conditionTier.getKey()));
                        indent =poolValues.size() > 1 ?  "      " : "    ";
                    }
                    for (Map.Entry<List<CardScaler.Filter.Config>, Double> kk:  conditionTier.getValue().entrySet()) {
                        var condTierList = kk.getKey();
                        var condTierWeight = kk.getValue();
                        if (!equalWeight(conditionTier.getValue())) {
                            ret.add(new TextComponent(indent+"WEIGHT: " + condTierWeight).withStyle(ChatFormatting.GRAY));
                        }
                        boolean debug = Config.DEBUG_CARDS.get();
                        for (CardScaler.Filter.Config condTier: condTierList) {
                            var condT = (CardScalerFilterConfigAccessor)condTier;
                            if (debug || condT.getColorFilter().keySet().stream().filter(Objects::nonNull).flatMap(Collection::stream).anyMatch(Objects::nonNull)) {
                                ret.add(new TextComponent(indent+"COLOR: ").append(formatInlineWeightedList(condT.getColorFilter())));
                            }
                            if (debug || condT.getGroupFilter().keySet().stream().filter(Objects::nonNull).flatMap(Collection::stream).anyMatch(Objects::nonNull)) {
                                ret.add(new TextComponent(indent+"GROUP: ").append(formatInlineWeightedList(condT.getGroupFilter())));
                            }
                            if (debug || condT.getNeighborFilter().keySet().stream().filter(Objects::nonNull).flatMap(Collection::stream).anyMatch(Objects::nonNull)) {
                                ret.add(new TextComponent(indent+"NEIGHBOR: ").append(formatInlineWeightedList(condT.getNeighborFilter())));
                            }
                            if (debug || condT.getTierFilter().keySet().stream().filter(Objects::nonNull).flatMap(Collection::stream).anyMatch(Objects::nonNull)) {
                                ret.add(new TextComponent(indent+"TIER: ").append(formatInlineWeightedList(condT.getTierFilter())));
                            }
                            ret.add(new TextComponent(""));
                        }
                    }
                }
            }
        }
        return ret;
    }

    public static List<Component> getTaskList() {
        List<Component> ret = new ArrayList<>();

        ret.add(new TextComponent("TASKS").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.AQUA));
        ret.add(new TextComponent(""));

        var tasks = (CardTasksConfigAccessor)ModConfigs.CARD_TASKS;
        var taskPools = tasks.getPools();
        var taskValues  = tasks.getValues();
        for (var pool: taskPools.entrySet()) {
            var poolKey = pool.getKey();
            var poolValues = pool.getValue();
            ret.add(new TextComponent(poolKey));
            boolean showWeight = Config.SHOW_WEIGHT.get() && !equalWeight(poolValues);
            for (var poolValue: poolValues.entrySet()) {
                Task task = taskValues.get(poolValue.getKey());
                if (task.getRenderer() instanceof CardTaskRenderer cardTaskRenderer) {
                    Component tooltip = cardTaskRenderer.getTooltip().copy();
                    ProgressConfiguredTask<?,?> progressTask = (ProgressConfiguredTask<?,?>) task.streamSelfAndDescendants().filter(ProgressConfiguredTask.class::isInstance).findFirst().orElse(null);
                    if (progressTask != null){
                        var targetCountConfig = progressTask.getCounter().getConfig() instanceof TargetTaskCounter.Config<?,?> ttc ? ttc : null;
                        if (targetCountConfig != null) {
                            IntRoll targetRoll = targetCountConfig.getTarget() instanceof IntRoll intRoll ? intRoll : null;
                            if (targetRoll != null) {
                                int minTarget = targetRoll.getMin();
                                int maxTarget = targetRoll.getMax();

                                String targetString = Objects.equals(minTarget, maxTarget) ? String.valueOf(minTarget) : minTarget + "-" + maxTarget;
                                tooltip = replace(tooltip, "${current}", new TextComponent("0"));
                                tooltip = replace(tooltip, "${target}", new TextComponent(targetString));
                            }
                        }
                    }
                    var tt = new TextComponent("    ").append(tooltip);
                    if (showWeight) {
                        tt.append(new TextComponent("    WEIGHT: " + poolValue.getValue()).withStyle(ChatFormatting.GRAY));
                    }
                    ret.add(tt);
                } else {
                    ret.add(new TextComponent("     UNKNOWN TASK TYPE" + task).withStyle(ChatFormatting.RED));
                }
            }
            ret.add(new TextComponent(""));
        }
        return ret;
    }

    // green if reference is passed (starts with @)
    private static MutableComponent createComponent(String str) {
        if (str == null) {
            return new TextComponent("NULL");
        }
        if (str.startsWith("@")){
            return new TextComponent(str).withStyle(ChatFormatting.GREEN).withStyle(ChatFormatting.ITALIC);
        }
        return new TextComponent(str);
    }


    public static Component replace(Component component, String target, TextComponent replacement) {
        if (!(component instanceof TextComponent base)) {
            return component;
        } else {
            List<Component> siblings = base.getSiblings();
            siblings.add(0, base.plainCopy().setStyle(base.getStyle()));

            for (int result = 0; result < siblings.size(); result++) {
                Component sibling = siblings.get(result);
                if (sibling instanceof TextComponent) {
                    String text = ((TextComponent)sibling).getText();
                    if (!text.isEmpty()) {
                        List<Component> parts = new ArrayList<>();
                        Style styledReplacement = replacement.getStyle() == Style.EMPTY ? sibling.getStyle() : Style.EMPTY;
                        if (text.equals(target)) {
                            parts.add(replacement.plainCopy().withStyle(styledReplacement));
                        } else {
                            for (String raw : text.split(Pattern.quote(target))) {
                                parts.add(new TextComponent(raw).setStyle(sibling.getStyle()));
                                parts.add(replacement.plainCopy().withStyle(styledReplacement));
                            }

                            parts.remove(parts.size() - 1);
                        }

                        siblings.remove(result);

                        for (int j = 0; j < parts.size(); j++) {
                            siblings.add(result, parts.get(parts.size() - j - 1));
                        }
                    }
                }
            }

            TextComponent result = new TextComponent("");
            result.setStyle(base.getStyle());

            for (Component sibling : siblings) {
                result.append(sibling);
            }

            return result;
        }
    }


    /**
     * returns true if all elements of weighted list have the same weight
     */
    private static boolean equalWeight(WeightedList<?> list) {
        Double lastWeight = null;
        for (var weight: list.values()) {
            if (lastWeight == null) {
                lastWeight = weight;
                continue;
            }
            if (lastWeight.doubleValue() != weight) {
                return false;
            }
        }
        return true;
    }

    private static String processIntroll(IntRoll intRoll) {
        if (intRoll == null) {
            return null;
        }
        if (intRoll instanceof IntRoll.Constant constant) {
            return constant.getCount()+"";
        }
        if (intRoll instanceof IntRoll.Uniform uniform) {
            if (uniform.getMin() == uniform.getMax()) {
                return uniform.getMin()+"";
            }
            return uniform.getMin() + "-" + uniform.getMax();
        }
        return "UNSUPPORTED INT ROLL " + intRoll;
    }

    private static TextComponent formatInlineWeightedList(WeightedList<?> weightedList){
        if (weightedList == null) {
            return new TextComponent("null");
        }
        if (weightedList.isEmpty()) {
            return new TextComponent(weightedList.toString());
        }
        if (weightedList.size() == 1) {
            var entry = weightedList.entrySet().stream().toList().get(0);
            if (entry.getValue() > 0 && entry.getKey() instanceof Set set && set.size() == 1) {
                return new TextComponent(""+set.iterator().next());
            }
            return entry != null && entry.getValue() > 0 ? new TextComponent( "" + entry.getKey()) : new TextComponent("{}");
        }
        var showWeight = Config.SHOW_WEIGHT.get() && !equalWeight(weightedList);
        TextComponent ret = new TextComponent("{");
        var totalWeight = weightedList.getTotalWeight();

        var it = weightedList.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            if (entry.getValue() > 0) {
                if (entry.getKey() instanceof Set set && set.size() == 1) {
                    ret.append(new TextComponent(""+set.iterator().next()));
                } else {
                    ret.append(new TextComponent(""+entry.getKey()));
                }
                if (showWeight) {
                    ret.append(new TextComponent(" " + new DecimalFormat("0.##").format(100*entry.getValue() / totalWeight) + "%").withStyle(ChatFormatting.GRAY));
                }
                if (it.hasNext()) {
                    ret.append(new TextComponent(" | "));
                }
            }
        }

        ret.append(new TextComponent("}"));
        return ret;
    }


    private static MutableComponent conditionText(CardConditionFilterConfigAccessor filter) {
        List<Component> parts = new ArrayList<>();
        if (filter.getColorFilter().keySet().stream().filter(Objects::nonNull).flatMap(Collection::stream).anyMatch(Objects::nonNull)) {
            parts.add(formatInlineWeightedList(filter.getColorFilter()));
        }

        if (filter.getNeighborFilter().keySet().stream().filter(Objects::nonNull).flatMap(Collection::stream).anyMatch(Objects::nonNull)) {
            parts.add(formatInlineWeightedList(filter.getNeighborFilter()));
        }

        if (filter.getGroupFilter().keySet().stream().filter(Objects::nonNull).flatMap(Collection::stream).anyMatch(Objects::nonNull)) {
            parts.add(formatInlineWeightedList(filter.getGroupFilter()));
        }

        if (filter.getTierFilter().keySet().stream().filter(Objects::nonNull).flatMap(Collection::stream).anyMatch(Objects::nonNull)) {
            parts.add(formatInlineWeightedList(filter.getTierFilter())); // TODO: this is not the best, shows {1,2,3,4} instead of 1-4
        }

        var minCnt = processIntroll(filter.getMinCount());
        var maxCnt = processIntroll(filter.getMaxCount());
        boolean singular = false;
        if ("1".equals(minCnt) && "1".equals(maxCnt)) singular = true;
        if (minCnt == null && "1".equals(maxCnt)) singular = true;
        if ("1".equals(minCnt) && maxCnt == null) singular = true;


        MutableComponent text = new TextComponent("").withStyle(ChatFormatting.WHITE);
        if (processIntroll(filter.getMinCount()) != null && processIntroll(filter.getMaxCount()) != null) {
            if (filter.getMinCount().equals(filter.getMaxCount())) {
                text.append(new TextComponent(singular ? "If there is exactly " : "If there are exactly ").withStyle(ChatFormatting.GRAY))
                    .append(new TextComponent(processIntroll(filter.getMinCount())+ " "));
            } else {
                text.append(new TextComponent("If there are between ").withStyle(ChatFormatting.GRAY))
                    .append(new TextComponent(processIntroll(filter.getMinCount()) + " "))
                    .append(new TextComponent("and "))
                    .append(new TextComponent(processIntroll(filter.getMaxCount()) + " "));
            }
        } else if (processIntroll(filter.getMinCount())!= null) {
            text.append(new TextComponent(singular ? "If there is at least " : "If there are at least ").withStyle(ChatFormatting.GRAY))
                .append(new TextComponent(processIntroll(filter.getMinCount()) + " "));
        } else if (processIntroll(filter.getMaxCount()) != null) {
            text.append(new TextComponent(singular ? "If there is at most " : "If there are at most ").withStyle(ChatFormatting.GRAY))
                .append(new TextComponent(processIntroll(filter.getMaxCount()) + " "));
        }

        for (int i = 0; i < parts.size(); i++) {
            text.append(parts.get(i));
            if (i != parts.size() - 1) {
                text.append(new TextComponent(", ").withStyle(ChatFormatting.GRAY));
            }
        }

        if (minCnt == null && maxCnt == null) {
            return null;
        }
        if (singular) {
            text.append(new TextComponent(parts.isEmpty() ? "Card" : " Card").withStyle(ChatFormatting.GRAY));
        } else {
            text.append(new TextComponent(parts.isEmpty() ? "Cards" : " Cards").withStyle(ChatFormatting.GRAY));
        }

        return text;

    }
}
