package com.radimous.vhatcaniroll.logic;

import com.radimous.vhatcaniroll.Config;
import com.radimous.vhatcaniroll.logic.modifiervalues.ModifierValues;
import com.radimous.vhatcaniroll.mixin.accessors.cards.*;
import iskallia.vault.config.card.BoosterPackConfig;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.core.card.CardCondition;
import iskallia.vault.core.card.CardEntry;
import iskallia.vault.core.card.CardProperty;
import iskallia.vault.core.card.CardScaler;
import iskallia.vault.core.card.modifier.card.*;
import iskallia.vault.core.card.modifier.deck.BountyDeckModifier;
import iskallia.vault.core.card.modifier.deck.DeckModifier;
import iskallia.vault.core.card.modifier.deck.DummyDeckModifier;
import iskallia.vault.core.card.modifier.deck.SlotDeckModifier;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.world.loot.entry.ItemLootEntry;
import iskallia.vault.core.world.roll.FloatRoll;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.text.DecimalFormat;
import java.util.*;

import static com.radimous.vhatcaniroll.VHatCanIRoll.ERROR_STYLE;

public class CardRolls {

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.##");

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
                ret.add(new TextComponent("  id: " + kk));
                ret.add(new TextComponent("  Model: ").append(vvA.getModel().getUnopened()));
            }
            ret.add(new TextComponent("  Card Count: ").append(formatInlineWeightedList(vvA.getRoll())));
            var col = vvA.getColor();
            if (
                col != null
                    && !equalWeight(col)
                    && (col.size() == 4 // hide all four at equal weight
                        && col.contains(CardEntry.Color.RED)
                        && col.contains(CardEntry.Color.GREEN)
                        && col.contains(CardEntry.Color.BLUE)
                        && col.contains(CardEntry.Color.YELLOW)
                )
            ) {
                ret.add(new TextComponent("  Card Color: ").append(formatInlineWeightedList(vvA.getColor())));
            }
            ret.add(new TextComponent("  Card Tier: ").append(formatInlineWeightedList(vvA.getTier())));
            ret.add(new TextComponent("  Cards:"));
            boolean showWeight = Config.SHOW_CARD_WEIGHT.get() && !equalWeight(vv.getCard());
            boolean showChance = Config.SHOW_CARD_CHANCE.get() && !equalWeight(vv.getCard());
            for (var pool : vv.getCard().entrySet()) {
                var poolKey = pool.getKey();
                var poolWeight = pool.getValue();
                if (showWeight && showChance) {
                    ret.add(new TextComponent("    Chance: "+DECIMAL_FORMAT.format(100* poolWeight / vv.getCard().getTotalWeight()) + "%" + " Weight: " + poolWeight).withStyle(ChatFormatting.GRAY));
                } else if (showChance) {
                    ret.add(new TextComponent("    Chance: "+DECIMAL_FORMAT.format(100* poolWeight / vv.getCard().getTotalWeight()) + "%").withStyle(ChatFormatting.GRAY));
                } else if (showWeight) {
                    ret.add(new TextComponent("    Weight: "+poolWeight).withStyle(ChatFormatting.GRAY));
                }
                for (BoosterPackConfig.CardConfig cardConfig: poolKey) {
                    var modifier = cardConfig.getModifier();
                    var cardConfigA = (BoosterPackConfigCardConfigAccessor) cardConfig;
                    WeightedList<List<CardEntry.Color>> cardColor = cardConfigA.getColors();
                    String cardCondition = cardConfigA.getCondition();
                    Set<String> cardGroups = cardConfigA.getGroups();
                    String cardScaler = cardConfigA.getScaler();
                    double chance = cardConfigA.getProbability();

                    ret.add(new TextComponent("    Modifier: ").append(createComponent(modifier)));
                    if (cardColor != null || Config.DEBUG_CARDS.get()) {
                        ret.add(new TextComponent("    Color: ").append(formatInlineWeightedList(cardColor)));
                    }
                    if (cardGroups != null || Config.DEBUG_CARDS.get()) {
                        ret.add(new TextComponent("    Groups: "+ cardGroups));
                    }
                    if ((cardCondition != null && !cardCondition.isEmpty()) || Config.DEBUG_CARDS.get()) {
                        ret.add(new TextComponent("    Condition: ").append(createComponent(cardCondition)));
                    }
                    if ((cardScaler != null && !cardScaler.isEmpty())|| Config.DEBUG_CARDS.get()) {
                        ret.add(new TextComponent("    Scaler: ").append(createComponent(cardScaler)));
                    }
                    if (chance != 1) {
                        ret.add(new TextComponent("    Chance: "+ chance));
                    }
                }
                ret.add(new TextComponent(""));
            }
        }

        return ret;
    }

    public static Collection<String> getModifierPools() {
        var modifiers = ModConfigs.CARD_MODIFIERS;
        var modPools = modifiers.getPools();
        return Collections.unmodifiableCollection(modPools.keySet());
    }

    public static List<Component> getModifierList(String modifierPool) {
        List<Component> ret = new ArrayList<>();
        ret.add(new TextComponent("MODIFIERS").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.AQUA));
        ret.add(new TextComponent(""));
        if (modifierPool == null) {
            ret.add(new TextComponent("Select modifier pool from the list on the right side =>").withStyle(ChatFormatting.YELLOW));
        }

        var modifiers = ModConfigs.CARD_MODIFIERS;

        var modPools = modifiers.getPools();
        var poolVal = modPools.get(modifierPool);
        if (poolVal == null) {
            ret.add(new TextComponent("modifier pool " + modifierPool + " not found").withStyle(ERROR_STYLE));
        }
        var modVals = modifiers.getValues();

        ret.add(new TextComponent(modifierPool));
        boolean showWeight = Config.SHOW_CARD_WEIGHT.get() && !equalWeight(poolVal);
        boolean showChance = Config.SHOW_CARD_CHANCE.get() && !equalWeight(poolVal);
        for (var cardRoll: poolVal.entrySet()) {
            var cardId = cardRoll.getKey();
            var cardWeight = cardRoll.getValue();
            var cardConfig = modVals.get(cardId);
            if (Config.DEBUG_CARDS.get()) {
                var rollCmp = new TextComponent("  id: "+ cardId);
                ret.add(rollCmp);
            }
            if (cardConfig == null) {
                ret.add(new TextComponent("  ERR: config for "+cardId + " card is null").withStyle(ERROR_STYLE));
                continue;
            }
            var cardColor = cardConfig.colors;
            var cardCondition = cardConfig.condition;
            var cardModel = cardConfig.model;
            var cardGroups = cardConfig.groups;
            var cardScaler = cardConfig.scaler;
            var cardValue = cardConfig.value;
            var cardName = cardConfig.name;

            var nameCmp = new TextComponent("  ").append(cardName);
            if (showWeight && showChance) {
                nameCmp.append(new TextComponent("  Chance: "+DECIMAL_FORMAT.format(100* cardWeight / poolVal.getTotalWeight()) + "%" + " Weight: " + cardWeight).withStyle(ChatFormatting.GRAY));
            } else if (showChance) {
                nameCmp.append(new TextComponent("  Chance: "+DECIMAL_FORMAT.format(100* cardWeight / poolVal.getTotalWeight()) + "%").withStyle(ChatFormatting.GRAY));
            } else if (showWeight) {
                nameCmp.append(new TextComponent("  Weight: "+cardWeight).withStyle(ChatFormatting.GRAY));
            }
            ret.add(nameCmp);

            if (Config.DEBUG_CARDS.get()) {
                ret.add(new TextComponent("    Model: "+ cardModel));
            }
            if ((cardColor != null && !cardColor.isEmpty()) || Config.DEBUG_CARDS.get()) {
                ret.add(new TextComponent("    Color: ").append(coloredSet(cardColor)));
            }
            if (cardGroups !=null || Config.DEBUG_CARDS.get()) {
                ret.add(new TextComponent("    Groups: "+ cardGroups));
            }
            if (cardCondition != null || Config.DEBUG_CARDS.get()) {
                ret.add(new TextComponent("    Condition: "+ cardCondition));
            }
            if (cardScaler !=null || Config.DEBUG_CARDS.get()) {
                ret.add(new TextComponent("    Scaler: "+ cardScaler));
            }
            if (cardValue instanceof GearCardModifier<?> gearCardModifier) {
                CardProperty.Config gearCardConfig = gearCardModifier.getConfig();
                if (gearCardConfig instanceof CardModifier.Config modifierConfig) {
                    int maxTier = modifierConfig.maxTier;
                    if (Config.DEBUG_CARDS.get()) {
                        ret.add(new TextComponent("    Max Tier: "+ maxTier));
                    }
                }
                if (gearCardConfig instanceof GearCardModifier.Config<?> gearModifierConfig) {
                    VaultGearAttribute<?> atr = gearModifierConfig.getAttribute();
                    Map<Integer, String> configPool = gearModifierConfig.getPool();
                    for (Map.Entry<Integer, String> configPoolVal: configPool.entrySet()) {
                        var singleTierArray = new ArrayList<VaultGearTierConfig.ModifierTier<?>>();
                        singleTierArray.add(new VaultGearTierConfig.ModifierTier<>(0,0, gearModifierConfig.getConfig(configPoolVal.getKey())));
                        var modComp = ModifierValues.getModifierComponent(atr, singleTierArray);
                        ret.add(new TextComponent("    T" + configPoolVal.getKey() + ": ").append(modComp));
                    }

                }
            } else if (cardValue instanceof TaskLootCardModifier taskLootCardModifier) {
                TaskLootCardModifier.Config taskLootConfig = taskLootCardModifier.getConfig();
                var taskLootConfigA = (TaskLootCardModifierConfigAccessor)taskLootConfig;
                if (Config.DEBUG_CARDS.get()) {
                    ret.add(new TextComponent("    Max Tier: " + taskLootConfig.maxTier));
                    ret.add(new TextComponent("    Task: ").append(createComponent(taskLootConfigA.getTask())));
                    var lootItems = taskLootConfigA.getLoot().getChildren();
                    if (lootItems.size() == 1) {
                        var item = lootItems.entrySet().stream().findFirst().orElse(null);
                        if (item != null) {
                            if (item.getKey() instanceof ItemLootEntry itemLootEntry) {
                                ret.add(new TextComponent("    Loot: ").append(itemLootEntry.getItem().getName(new ItemStack(itemLootEntry.getItem()))));
                            } else {
                                ret.add(new TextComponent("    Loot: UNSUPPORTED (non Item loot)").withStyle(ERROR_STYLE));
                            }
                        } else {
                            ret.add(new TextComponent("    Loot: UNSUPPORTED (single NULL)").withStyle(ERROR_STYLE));
                        }
                    } else {
                        ret.add(new TextComponent("    Loot: UNSUPPORTED (not a single item)").withStyle(ERROR_STYLE));
                    }
                    if (taskLootConfigA.getCount().size() == 1) {
                        var cnt = taskLootConfigA.getCount().entrySet().stream().findFirst().orElse(null);
                        if (cnt != null) {
                            ret.add(new TextComponent("    Count: " + processIntroll(cnt.getValue())));
                        } else {
                            ret.add(new TextComponent("    Count: UNSUPPORTED (null cnt)").withStyle(ERROR_STYLE));
                        }
                    } else {
                        ret.add(new TextComponent("    Count:"));
                        for (var cnt : taskLootConfigA.getCount().entrySet()) {
                            ret.add(new TextComponent("                 - "+cnt.getKey()+ " => " + processIntroll(cnt.getValue())));
                        }
                    }
                    ret.add(new TextComponent("    Highlight: " + taskLootConfigA.getHighlightColor()));
                    ret.add(new TextComponent("    Tooltip: ").append(taskLootConfigA.getTooltip()));
                }
                var tt = taskLootConfigA.getTooltip().copy();
                var cnt = taskLootConfigA.getCount().entrySet().stream().findFirst().orElse(null);
                if (cnt != null) {
                    String roll = processIntroll(cnt.getValue());
                    if (roll != null) {
                        tt = (MutableComponent) ComponentUtil.replace(tt, "${count}", new TextComponent(roll));
                    }
                } else {
                    ret.add(new TextComponent("    Count: UNSUPPORTED (null cnt)").withStyle(ERROR_STYLE));
                }
                tt = (MutableComponent) ComponentUtil.replace(tt, "${task}", (TextComponent) createComponent(taskLootConfigA.getTask()));
                ret.add(new TextComponent("    ").append(tt));

            } else if (cardValue instanceof GreedCardModifier greedCardModifier) {
                GreedCardModifier.Config greedCardConfig = greedCardModifier.getConfig();
                var greedCardConfigA = (GreedCardModifierConfigAccessor)greedCardConfig;

                float mpMin = greedCardConfigA.getMultiplierPool().values().stream().map(FloatRoll::getMin).min(Float::compare).orElse(-1F);
                float mpMax = greedCardConfigA.getMultiplierPool().values().stream().map(FloatRoll::getMax).max(Float::compare).orElse(-1F);
                if (Config.DEBUG_CARDS.get()) {
                    ret.add(new TextComponent("    Max Tier: " + greedCardConfig.maxTier));
                    ret.add(new TextComponent("    Neighbors: " + greedCardConfigA.getTargetNeighborTypes()));
                    ret.add(new TextComponent("    Multiplier: " + mpMin + " - " + mpMax));
                    ret.add(new TextComponent("    Colors: " + greedCardConfigA.getTargetColorFilter()));
                    ret.add(new TextComponent("    Groups: " + greedCardConfigA.getTargetGroupFilter()));
                }
                MutableComponent text = new TextComponent("x" + String.format("%.1f", mpMin)).setStyle(Style.EMPTY.withColor(14329120));;
                text.append(new TextComponent("-"));
                text.append(new TextComponent("x" + String.format("%.1f", mpMax)).setStyle(Style.EMPTY.withColor(14329120)));
                text.append(new TextComponent(" to ").withStyle(ChatFormatting.GRAY));

                List<Component> filterParts = new ArrayList<>();
                if (greedCardConfigA.getTargetColorFilter() != null && !greedCardConfigA.getTargetColorFilter().isEmpty()) {
                    greedCardConfigA.getTargetColorFilter()
                        .stream()
                        .map(CardEntry.Color::getColoredText)
                        .reduce((c1, c2) -> new TextComponent("").append(c1).append(new TextComponent("/").withStyle(ChatFormatting.GRAY)).append(c2))
                        .ifPresent(filterParts::add);
                }

                if (greedCardConfigA.getTargetGroupFilter() != null && !greedCardConfigA.getTargetGroupFilter().isEmpty()) {
                    greedCardConfigA.getTargetGroupFilter()
                        .stream()
                        .map(s -> new TextComponent(s).withStyle(ChatFormatting.WHITE))
                        .reduce((c1, c2) -> new TextComponent("").append(c1).append(new TextComponent("/").withStyle(ChatFormatting.GRAY)).append(c2))
                        .ifPresent(filterParts::add);
                }

                for (int i = 0; i < filterParts.size(); i++) {
                    text.append(filterParts.get(i));
                    if (i < filterParts.size() - 1) {
                        text.append(new TextComponent(" ").withStyle(ChatFormatting.GRAY));
                    }
                }

                if (filterParts.isEmpty()) {
                    text.append(new TextComponent("Cards").withStyle(ChatFormatting.WHITE));
                } else {
                    text.append(new TextComponent(" Cards").withStyle(ChatFormatting.GRAY));
                }

                if (!((GreedCardModifierConfigAccessor) greedCardConfig).getTargetNeighborTypes().isEmpty()) {
                    text.append(new TextComponent(" ").withStyle(ChatFormatting.GRAY));
                    ((GreedCardModifierConfigAccessor) greedCardConfig).getTargetNeighborTypes()
                        .stream()
                        .map(type -> type.getText().withStyle(ChatFormatting.WHITE))
                        .reduce((c1, c2) -> new TextComponent("").append(c1).append(new TextComponent("/").withStyle(ChatFormatting.GRAY)).append(c2))
                        .ifPresent(text::append);
                }

                ret.add(new TextComponent("    ").append(text));
            } else if (cardValue instanceof DummyCardModifier) {
                // WHY IS THIS EVEN A THING
            } else {
                ret.add(new TextComponent("    UNKNOWN CARD VALUE: "+ cardValue).withStyle(ERROR_STYLE));
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
                CardCondition.Config conditionConfig = condition.getConfig();
                var conditionConfigA = (CardConditionConfigAccessor) conditionConfig;
                var conditionTiers = conditionConfigA.getTiers();
                ret.add(new TextComponent("  " + poolValue.getKey()));
                if (!equalWeight(poolValues)) {
                    ret.add(new TextComponent("    Weight: " + poolValue.getValue()).withStyle(ChatFormatting.GRAY));
                }
                for (var conditionTier : conditionTiers.entrySet()) {
                    if (conditionTiers.size() > 1) {
                        ret.add(new TextComponent("     Tier: "+conditionTier.getKey()));
                    }
                    boolean showWeight = Config.SHOW_CARD_WEIGHT.get() && !equalWeight(conditionTier.getValue());
                    boolean showChance = Config.SHOW_CARD_CHANCE.get() && !equalWeight(conditionTier.getValue());
                    for (Map.Entry<List<CardCondition.Filter.Config>, Double> kk:  conditionTier.getValue().entrySet()) {
                        var condTierList = kk.getKey();
                        var condTierWeight = kk.getValue();
                        if (showWeight && showChance) {
                            ret.add(new TextComponent("     Chance: "+DECIMAL_FORMAT.format(100* condTierWeight / conditionTier.getValue().getTotalWeight()) + "%" + " Weight: " + condTierWeight).withStyle(ChatFormatting.GRAY));
                        } else if (showChance) {
                            ret.add(new TextComponent("     Chance: "+DECIMAL_FORMAT.format(100* condTierWeight / conditionTier.getValue().getTotalWeight()) + "%").withStyle(ChatFormatting.GRAY));
                        } else if (showWeight) {
                            ret.add(new TextComponent("     Weight: "+condTierWeight).withStyle(ChatFormatting.GRAY));
                        }
                        for (CardCondition.Filter.Config condTier: condTierList) {
                            var condT = (CardConditionFilterConfigAccessor)condTier;
                            if (Config.DEBUG_CARDS.get()) {
                                ret.add(new TextComponent("         Color: ").append(formatInlineWeightedList(condT.getColorFilter())));
                                ret.add(new TextComponent("         Group: ").append(formatInlineWeightedList(condT.getGroupFilter())));
                                ret.add(new TextComponent("         Neighbor: ").append(formatInlineWeightedList(condT.getNeighborFilter())));
                                ret.add(new TextComponent("         Tier: ").append(formatInlineWeightedList(condT.getTierFilter())));
                                ret.add(new TextComponent("         Min Count: " + processIntroll(condT.getMinCount())));
                                ret.add(new TextComponent("         Max Count: " + processIntroll(condT.getMaxCount())));
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
            ret.add(new TextComponent(poolKey));
            boolean showPoolWeight = Config.SHOW_CARD_WEIGHT.get() && !equalWeight(poolValues);
            boolean showPoolChance = Config.SHOW_CARD_CHANCE.get() && !equalWeight(poolValues);
            for (var poolValue: poolValues.entrySet()) {
                if (poolValues.size() > 1) {
                    var pvCmp = new TextComponent("  Entry: " + poolValue.getKey());
                    if (showPoolWeight && showPoolChance) {
                        pvCmp.append(new TextComponent("  Chance: "+DECIMAL_FORMAT.format(100* poolValue.getValue() / poolValues.getTotalWeight()) + "%" + " Weight: " + poolValue.getValue()).withStyle(ChatFormatting.GRAY));
                    } else if (showPoolChance) {
                        pvCmp.append(new TextComponent("  Chance: "+DECIMAL_FORMAT.format(100* poolValue.getValue() / poolValues.getTotalWeight()) + "%").withStyle(ChatFormatting.GRAY));
                    } else if (showPoolWeight) {
                        pvCmp.append(new TextComponent("  Weight: "+poolValue.getValue()).withStyle(ChatFormatting.GRAY));
                    }
                    ret.add(pvCmp);
                }
                CardScaler scalerVal = scalerValues.get(poolValue.getKey());
                CardScaler.Config scalerConfig = scalerVal.getConfig();
                var scalerConfigA = (CardScalerConfigAccessor) scalerConfig;
                for (Map.Entry<Integer, WeightedList<List<CardScaler.Filter.Config>>> conditionTier : scalerConfigA.getTiers().entrySet()) {
                    String indent = poolValues.size() > 1 ? "    " : "  ";
                    if (scalerConfigA.getTiers().size() > 1) {
                        ret.add(new TextComponent("    Tier: "+conditionTier.getKey()));
                        indent =poolValues.size() > 1 ?  "      " : "    ";
                    }
                    boolean showConditionWeight = Config.SHOW_CARD_WEIGHT.get() && !equalWeight(conditionTier.getValue());
                    boolean showConditionChance = Config.SHOW_CARD_CHANCE.get() && !equalWeight(conditionTier.getValue());
                    for (Map.Entry<List<CardScaler.Filter.Config>, Double> kk:  conditionTier.getValue().entrySet()) {
                        var condTierList = kk.getKey();
                        var condTierWeight = kk.getValue();
                        if (showConditionWeight && showConditionChance) {
                            ret.add(new TextComponent("  Chance: "+DECIMAL_FORMAT.format(100* condTierWeight / conditionTier.getValue().getTotalWeight()) + "%" + " Weight: " + condTierWeight).withStyle(ChatFormatting.GRAY));
                        } else if (showConditionChance) {
                            ret.add(new TextComponent("  Chance: "+DECIMAL_FORMAT.format(100* condTierWeight / conditionTier.getValue().getTotalWeight()) + "%").withStyle(ChatFormatting.GRAY));
                        } else if (showConditionWeight) {
                            ret.add(new TextComponent("  Weight: "+condTierWeight).withStyle(ChatFormatting.GRAY));
                        }
                        for (CardScaler.Filter.Config condTier: condTierList) {
                            var condT = (CardScalerFilterConfigAccessor)condTier;
                            ret.add(new TextComponent("  ").append(scalerText(condT)));
                            if (Config.DEBUG_CARDS.get()) {
                                ret.add(new TextComponent(indent+"Color: ").append(formatInlineWeightedList(condT.getColorFilter())));
                                ret.add(new TextComponent(indent+"Group: ").append(formatInlineWeightedList(condT.getGroupFilter())));
                                ret.add(new TextComponent(indent+"Neighbor: ").append(formatInlineWeightedList(condT.getNeighborFilter())));
                                ret.add(new TextComponent(indent+"Tier: ").append(formatInlineWeightedList(condT.getTierFilter())));
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
            if (poolKey.equals("default") && !Config.DEBUG_CARDS.get()) {
                continue; // just skip this config (it's only used by Ma Balls)
            }
            var poolValues = pool.getValue();
            ret.add(new TextComponent(poolKey));
            boolean showWeight = Config.SHOW_CARD_WEIGHT.get() && !equalWeight(poolValues);
            boolean showChance = Config.SHOW_CARD_CHANCE.get() && !equalWeight(poolValues);
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
                                tooltip = ComponentUtil.replace(tooltip, "${current}", new TextComponent("0"));
                                tooltip = ComponentUtil.replace(tooltip, "${target}", new TextComponent(targetString));
                            }
                        }
                    }
                    var tt = new TextComponent("  ").append(tooltip);
                    if (showWeight && showChance) {
                        tt.append(new TextComponent("  Chance: "+DECIMAL_FORMAT.format(100* poolValue.getValue() / poolValues.getTotalWeight()) + "%" + " Weight: " + poolValue.getValue()).withStyle(ChatFormatting.GRAY));
                    } else if (showChance) {
                        tt.append(new TextComponent("  Chance: "+DECIMAL_FORMAT.format(100* poolValue.getValue() / poolValues.getTotalWeight()) + "%").withStyle(ChatFormatting.GRAY));
                    } else if (showWeight) {
                        tt.append(new TextComponent("  Weight: "+poolValue.getValue()).withStyle(ChatFormatting.GRAY));
                    }
                    ret.add(tt);
                } else {
                    ret.add(new TextComponent("   UNKNOWN TASK TYPE" + task).withStyle(ERROR_STYLE));
                }
            }
            ret.add(new TextComponent(""));
        }
        return ret;
    }


    public static Collection<String> getDeckCorePools() {
        var modifiers = (DeckModifiersConfigAccessor) ModConfigs.DECK_MODIFIERS;
        var modPools = modifiers.getPools();
        return Collections.unmodifiableCollection(modPools.keySet());
    }


    public static List<Component> getDeckCoresList(String modifierPool) {
        List<Component> ret = new ArrayList<>();

        ret.add(new TextComponent("DECK CORES").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.AQUA));
        ret.add(new TextComponent(""));

        var modifiers = (DeckModifiersConfigAccessor)ModConfigs.DECK_MODIFIERS;
        var modifierPools = modifiers.getPools();
        var modifierValues  = modifiers.getValues();

        var poolValues = modifierPools.get(modifierPool);
        ret.add(new TextComponent(modifierPool));
        ret.add(new TextComponent(""));
        boolean showWeight = Config.SHOW_CARD_WEIGHT.get() && !equalWeight(poolValues);
        boolean showChance = Config.SHOW_CARD_CHANCE.get() && !equalWeight(poolValues);

        for (var poolValue: poolValues.entrySet()) {
            DeckModifier<?> modifier = modifierValues.get(poolValue.getKey());
            DeckModifier.Config config = modifier.getConfig();
            String modelId = modifier.getModelId();
            String id = modifier.getId();
            String name = modifier.getName();
            int color = modifier.getColour();
            ret.add(new TextComponent("    Model: " + modelId));
            if (Config.DEBUG_CARDS.get()) ret.add(new TextComponent("    ID: " + id));
            if (Config.DEBUG_CARDS.get()) ret.add(new TextComponent("    Type: " + modifier.getClass().getSimpleName()));
            ret.add(new TextComponent("    " + name).withStyle(Style.EMPTY.withColor(color)));
            if (config instanceof DummyDeckModifier.Config dummy) {
                ret.add(new TextComponent("    " + "DUMMY"));
            }
            Component ttipComponent = null;
            try {
                List<Component> ttips = new ArrayList<>();
                modifier.addText(ttips, 0, TooltipFlag.Default.NORMAL, 0);
                ttipComponent = ComponentUtil.replace(ttips.get(0), "-100.0%", new TextComponent("-100%"));
            } catch (Exception e) {
                System.out.println("ERR" + e.getMessage());
            }
            if (ttipComponent == null) {
                ret.add(new TextComponent("ERR - NULL TOOLTIP COMPONENT").withStyle(ERROR_STYLE));
                ret.add(new TextComponent(""));
                continue;
            }

            if (Wolds.cardRoll(ret, modifier, config, ttipComponent)){
                // handled in the cardRoll method
            } else if (config instanceof BountyDeckModifier.Config bountyConfig) {
                boolean moreThanOne = bountyConfig.bountyRolls.containsKey("lesser") || bountyConfig.bountyRolls.containsKey("greater");
                if (bountyConfig.bountyRolls.containsKey("lesser")) {
                    String tierIncreaseL = processFloatroll(bountyConfig.bountyRolls.get("lesser").getCrateTierIncrease(bountyConfig.crateTierIncrease));
                    String resourceCardsRequiredL = processIntroll(bountyConfig.bountyRolls.get("lesser").getResourceCardsRequired(bountyConfig.resourceCardsRequired));
                    MutableComponent comp = new TextComponent("     " + "Lesser: ").append(new TextComponent("+").withStyle(ChatFormatting.GRAY).append((new TextComponent(tierIncreaseL)).withStyle(ChatFormatting.WHITE)).append(" Crate Tiers Completing a vault with at least ").append((new TextComponent(resourceCardsRequiredL)).withStyle(ChatFormatting.WHITE)).append(" Resource Cards"));
                    ret.add(comp);
                }
                String tierIncrease = processFloatroll(bountyConfig.crateTierIncrease);
                String resourceCardsRequired = processIntroll(bountyConfig.resourceCardsRequired);
                MutableComponent comp = new TextComponent("     " + (moreThanOne ? "Normal: " : "")).append(new TextComponent("+").withStyle(ChatFormatting.GRAY).append((new TextComponent(tierIncrease)).withStyle(ChatFormatting.WHITE)).append(" Crate Tiers Completing a vault with at least ").append((new TextComponent(resourceCardsRequired)).withStyle(ChatFormatting.WHITE)).append(" Resource Cards"));
                ret.add(comp);
                if (bountyConfig.bountyRolls.containsKey("greater")) {
                    String tierIncreaseG = processFloatroll(bountyConfig.bountyRolls.get("greater").getCrateTierIncrease(bountyConfig.crateTierIncrease));
                    String resourceCardsRequiredG = processIntroll(bountyConfig.bountyRolls.get("greater").getResourceCardsRequired(bountyConfig.resourceCardsRequired));
                    MutableComponent compG = new TextComponent("     " + "Greater: ").append(new TextComponent("+").withStyle(ChatFormatting.GRAY).append((new TextComponent(tierIncreaseG)).withStyle(ChatFormatting.WHITE)).append(" Crate Tiers Completing a vault with at least ").append((new TextComponent(resourceCardsRequiredG)).withStyle(ChatFormatting.WHITE)).append(" Resource Cards"));
                    ret.add(compG);
                }
            } else if (config instanceof SlotDeckModifier.Config slotConfig && config.getClass() == SlotDeckModifier.Config.class) { // class check to ski wolds cores that extend the config
                boolean moreThanOne = slotConfig.slotRolls.containsKey("lesser") || slotConfig.slotRolls.containsKey("greater");
                if (slotConfig.slotRolls.containsKey("lesser")) {
                    String slotRollL = processIntroll(slotConfig.slotRolls.get("lesser").getSlotRoll(slotConfig.slotRoll));
                    Component colorRequiredL = coloredSet(slotConfig.slotRolls.get("lesser").getRequiredColors(slotConfig.requiredColors));
                    String groupsRequiredL = slotConfig.slotRolls.get("lesser").getRequiredGroups(slotConfig.requiredGroups).toString();
                    if ("[]".equals(groupsRequiredL)) groupsRequiredL = "";
                    if ("[]".equals(colorRequiredL.getString())) colorRequiredL = new TextComponent("");
                    MutableComponent comp = new TextComponent("     " + "Lesser: ").append((new TextComponent("+")).withStyle(ChatFormatting.GRAY).append((new TextComponent(processFloatrollPercent(config.modifierRoll))).withStyle(ChatFormatting.WHITE)).append(new TextComponent(" efficiency for ")).append((new TextComponent(slotRollL + " ")).withStyle(ChatFormatting.WHITE)));
                    comp.append(colorRequiredL).append(groupsRequiredL);
                    comp.append(" cards");
                    ret.add(comp);
                }
                String slotRoll = processIntroll(slotConfig.slotRoll);
                Component colorRequired = coloredSet(slotConfig.requiredColors);
                String groupsRequired = slotConfig.requiredGroups.toString();
                if ("[]".equals(groupsRequired)) groupsRequired = "";
                if ("[]".equals(colorRequired.getString())) colorRequired = new TextComponent("");
                MutableComponent comp = new TextComponent("     " + (moreThanOne ? "Normal: " : "")).append((new TextComponent("+")).withStyle(ChatFormatting.GRAY).append((new TextComponent(processFloatrollPercent(config.modifierRoll))).withStyle(ChatFormatting.WHITE)).append(new TextComponent(" efficiency for ")).append((new TextComponent(slotRoll + " ")).withStyle(ChatFormatting.WHITE)));
                comp.append(colorRequired).append(groupsRequired);
                comp.append(" cards");
                ret.add(comp);
                if (slotConfig.slotRolls.containsKey("greater")) {
                    String slotRollG = processIntroll(slotConfig.slotRolls.get("greater").getSlotRoll(slotConfig.slotRoll));
                    Component colorRequiredG = coloredSet(slotConfig.slotRolls.get("greater").getRequiredColors(slotConfig.requiredColors));
                    String groupsRequiredG = slotConfig.slotRolls.get("greater").getRequiredGroups(slotConfig.requiredGroups).toString();
                    if ("[]".equals(groupsRequiredG)) groupsRequiredG = "";
                    if ("[]".equals(colorRequiredG.getString())) colorRequiredG = new TextComponent("");
                    MutableComponent compG = new TextComponent("     " + "Greater: ").append((new TextComponent("+")).withStyle(ChatFormatting.GRAY).append((new TextComponent(processFloatrollPercent(config.modifierRoll))).withStyle(ChatFormatting.WHITE)).append(new TextComponent(" efficiency for ")).append((new TextComponent(slotRollG + " ")).withStyle(ChatFormatting.WHITE)));
                    compG.append(colorRequiredG).append(groupsRequiredG);
                    compG.append(" cards");
                    ret.add(compG);
                }
            } else if (config.modifierRolls.size() == 2 && config.modifierRolls.containsKey("lesser") && config.modifierRolls.containsKey("greater")) {
                ret.add(new TextComponent("     " + "Lesser: ").append(ComponentUtil.replace(ttipComponent.copy(), "-100%", new TextComponent(processFloatrollPercent(config.modifierRolls.get("lesser").roll)))));
                ret.add(new TextComponent("     " + "Normal: ").append(ComponentUtil.replace(ttipComponent.copy(), "-100%", new TextComponent(processFloatrollPercent(config.modifierRoll)))));
                ret.add(new TextComponent("     " + "Greater: ").append(ComponentUtil.replace(ttipComponent.copy(), "-100%", new TextComponent(processFloatrollPercent(config.modifierRolls.get("greater").roll)))));

            } else {
                ret.add(new TextComponent("     ").append(ComponentUtil.replace(ttipComponent.copy(), "-100%", new TextComponent(processFloatrollPercent(config.modifierRoll)))));
                for (var mr: config.modifierRolls.entrySet()) {
                    ret.add(new TextComponent("     " + mr.getKey() +": ").append(ComponentUtil.replace(ttipComponent.copy(), "-100%", new TextComponent(processFloatrollPercent(mr.getValue().roll)))));
                }
            }
            if (config.selectedRollId != null) ret.add(new TextComponent("    Selected Roll: " + config.selectedRollId));

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

    static String processIntroll(IntRoll intRoll) {
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

    private static String processFloatrollPercent(FloatRoll floatRoll) {
        if (floatRoll == null) {
            return null;
        }
        if (floatRoll instanceof FloatRoll.Constant constant) {
            return DECIMAL_FORMAT.format(constant.getCount()*100)+"%";
        }
        if (floatRoll instanceof FloatRoll.Uniform uniform) {
            if (uniform.getMin() == uniform.getMax()) {
                return DECIMAL_FORMAT.format(uniform.getMin()*100)+"%";
            }
            return DECIMAL_FORMAT.format(uniform.getMin()*100) + "%-" + DECIMAL_FORMAT.format(uniform.getMax()*100)+"%";
        }
        if (floatRoll instanceof FloatRoll.UniformedStep uniform) {
            if (uniform.getMin() == uniform.getMax()) {
                return DECIMAL_FORMAT.format(uniform.getMin()*100)+"%";
            }
            return DECIMAL_FORMAT.format(uniform.getMin()*100) + "%-" + DECIMAL_FORMAT.format(uniform.getMax()*100)+"%";
        }
        return "UNSUPPORTED FLOAT ROLL " + floatRoll;
    }
    static String processFloatroll(FloatRoll floatRoll) {
        if (floatRoll == null) {
            return null;
        }
        if (floatRoll instanceof FloatRoll.Constant constant) {
            return DECIMAL_FORMAT.format(constant.getCount());
        }
        if (floatRoll instanceof FloatRoll.Uniform uniform) {
            if (uniform.getMin() == uniform.getMax()) {
                return DECIMAL_FORMAT.format(uniform.getMin());
            }
            return DECIMAL_FORMAT.format(uniform.getMin()) + "-" + DECIMAL_FORMAT.format(uniform.getMax());
        }
        if (floatRoll instanceof FloatRoll.UniformedStep uniform) {
            if (uniform.getMin() == uniform.getMax()) {
                return DECIMAL_FORMAT.format(uniform.getMin());
            }
            return DECIMAL_FORMAT.format(uniform.getMin()) + "-" + DECIMAL_FORMAT.format(uniform.getMax());
        }
        return "UNSUPPORTED FLOAT ROLL " + floatRoll;
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
                return colorComponent(new TextComponent(""+set.iterator().next()));
            }
            return entry != null && entry.getValue() > 0 ? colorComponent(new TextComponent( "" + entry.getKey())) : new TextComponent("{}");
        }
        var showWeight = Config.SHOW_CARD_WEIGHT.get() && !equalWeight(weightedList);
        var showChance = Config.SHOW_CARD_CHANCE.get() && !equalWeight(weightedList);
        TextComponent ret = new TextComponent("{");
        var totalWeight = weightedList.getTotalWeight();

        var it = weightedList.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            if (entry.getValue() > 0) {
                if (entry.getKey() instanceof Set set && set.size() == 1) {
                    ret.append(colorComponent(new TextComponent(""+set.iterator().next())));
                } else {
                    ret.append(new TextComponent(""+entry.getKey()));
                }
                if (showWeight && showChance) {
                    ret.append(new TextComponent(" "+DECIMAL_FORMAT.format(100* entry.getValue() / totalWeight) + "%" + " W" + entry.getValue()).withStyle(ChatFormatting.GRAY));
                } else if (showChance) {
                    ret.append(new TextComponent(" "+DECIMAL_FORMAT.format(100* entry.getValue() / totalWeight) + "%").withStyle(ChatFormatting.GRAY));
                } else if (showWeight) {
                    ret.append(new TextComponent(" W"+entry.getValue()).withStyle(ChatFormatting.GRAY));
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
        if ((hasAnyNonNull(filter.getColorFilter()))) {
            parts.add(formatInlineWeightedList(filter.getColorFilter()));
        }

        if ((hasAnyNonNull(filter.getNeighborFilter()))) {
            parts.add(formatInlineWeightedList(filter.getNeighborFilter()));
        }

        if (hasAnyNonNull(filter.getGroupFilter())) {
            parts.add(formatInlineWeightedList(filter.getGroupFilter()));
        }

        if (hasAnyNonNull(filter.getTierFilter())) {
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

    private static MutableComponent scalerText(CardScalerFilterConfigAccessor filter) {
        List<Component> parts = new ArrayList<>();
        if (hasAnyNonNull(filter.getColorFilter())) {
            parts.add(formatInlineWeightedList(filter.getColorFilter()));
        }

        if (hasAnyNonNull(filter.getNeighborFilter())) {
            parts.add(formatInlineWeightedList(filter.getNeighborFilter()));
        }

        if (hasAnyNonNull(filter.getGroupFilter())) {
            parts.add(formatInlineWeightedList(filter.getGroupFilter()));
        }

        if (hasAnyNonNull(filter.getTierFilter())) {
            parts.add(formatInlineWeightedList(filter.getTierFilter()));
        }

        MutableComponent text = new TextComponent("");
        text.append((new TextComponent("For Each ")).withStyle(ChatFormatting.GRAY));

        for(int i = 0; i < parts.size(); ++i) {
            text.append(parts.get(i));
            if (i != parts.size() - 1) {
                text.append((new TextComponent(", ")).withStyle(ChatFormatting.GRAY));
            }
        }

        text.append((new TextComponent(parts.isEmpty() ? "Card" : " Card")).withStyle(ChatFormatting.GRAY));
        return text;
    }





    private static TextComponent colorComponent(TextComponent component) {

        if ("GREEN".equals(component.getContents())) {
            return (TextComponent) component.withStyle(CardEntry.Color.GREEN.getColoredText().getStyle());
        }
        if ("RED".equals(component.getContents())) {
            return (TextComponent) component.withStyle(CardEntry.Color.RED.getColoredText().getStyle());
        }
        if ("BLUE".equals(component.getContents())) {
            return (TextComponent) component.withStyle(CardEntry.Color.BLUE.getColoredText().getStyle());
        }
        if ("YELLOW".equals(component.getContents())) {
            return (TextComponent) component.withStyle(CardEntry.Color.YELLOW.getColoredText().getStyle());
        }
        return component;
    }

    private static TextComponent coloredSet(Set<CardEntry.Color> colors) {
        if (colors == null) {
            return new TextComponent("null");
        }
        var ret = new TextComponent("[");

        var it = colors.iterator();
        while (it.hasNext()) {
            var color = it.next();
            ret.append(colorComponent(new TextComponent(""+color)));

            if (it.hasNext()) {
                ret.append(new TextComponent(", "));
            }
        }
        ret.append("]");
        return ret;
    }

    private static <T>boolean hasAnyNonNull(WeightedList<Set<T>> weightedList) {
        return weightedList.keySet().stream().filter(Objects::nonNull).flatMap(Collection::stream).anyMatch(Objects::nonNull);
    }
}
