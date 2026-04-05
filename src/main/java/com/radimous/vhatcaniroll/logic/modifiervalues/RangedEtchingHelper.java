package com.radimous.vhatcaniroll.logic.modifiervalues;


import com.mojang.datafixers.util.Pair;
import iskallia.vault.gear.attribute.config.WeightedListAttributeGenerator;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.util.Throuple;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.text.DecimalFormat;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link iskallia.vault.gear.etching.EtchingHelper}
 */
public class RangedEtchingHelper {
    private static final Pattern CONDITIONAL_PATTERN = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)\\s*(>=|<=|!=|>|<|=)\\s*(-?\\d+(?:\\.\\d+)?)=(.*)");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    public static String formatDescription(String description, Optional<?> minValue, Optional<?> maxValue) {
        if (minValue.isEmpty() || maxValue.isEmpty()) {
            return description;
        }
        Object minVal = minValue.get();
        Object maxVal = maxValue.get();
        if (minVal instanceof Pair<?, ?> minPair && maxVal instanceof Pair<?, ?> maxPair) {
            description = formatDescription(description, Map.of("valueA", minPair.getFirst(), "valueB", minPair.getSecond()), Map.of("valueA", maxPair.getFirst(), "valueB", maxPair.getSecond()));
        } else if (minVal instanceof Throuple<?, ?, ?> minThr && maxVal instanceof Throuple<?, ?, ?> maxThr) {
            description = formatDescription(description,
                Map.of("valueA", minThr.getFirst(), "valueB", minThr.getSecond(), "valueC", minThr.getThird()),
                Map.of("valueA", maxThr.getFirst(), "valueB", maxThr.getSecond(), "valueC", maxThr.getThird()));
        } else {
            description = formatDescription(description, Map.of("value", minVal), Map.of("value", maxVal));
        }

        return description;
    }

    private static String formatDescription(String description, Map<String, ?> minValues, Map<String, ?> maxValues) {
        StringBuilder result = new StringBuilder();
        int index = 0;
        Map<String, Double> formattedMinNumbers = new HashMap<>();
        Map<String, Double> formattedMaxNumbers = new HashMap<>();

        while (index < description.length()) {
            int start = description.indexOf(37, index);
            if (start == -1 || start + 1 >= description.length()) {
                result.append(description.substring(index));
                break;
            }

            result.append(description, index, start);
            char nextChar = description.charAt(start + 1);
            if (nextChar == 'd') {
                String key = extractKey(description, start + 2);
                Object minObj = minValues.get(key);
                Object maxObj = maxValues.get(key);
                if (minObj instanceof Number minNumber && maxObj instanceof Number maxNumber) {
                    if (minNumber.equals(maxNumber)) {
                        result.append(minNumber);
                    } else {
                        result.append(minNumber).append("-").append(maxNumber);
                    }
                    formattedMinNumbers.put(key, minNumber.doubleValue());
                    formattedMaxNumbers.put(key, maxNumber.doubleValue());
                    index = start + 2 + key.length();
                    continue;
                }
            } else if (nextChar == 's') {
                String fullKey = extractKeyWithCase(description, start + 2);
                String caseKey = "";
                String key = fullKey;
                int slashIndex = fullKey.indexOf(47);
                if (slashIndex != -1) {
                    caseKey = fullKey.substring(slashIndex + 1);
                    key = fullKey.substring(0, slashIndex);
                }

                Object minObj = minValues.get(key);
                Object maxObj = maxValues.get(key);

                if (minObj instanceof WeightedListAttributeGenerator.Config<?> minListCfg && maxObj instanceof WeightedListAttributeGenerator.Config<?> maxListCfg) {
                    StringBuilder sb = new StringBuilder();
                    AbstractMap<?, Double> minList = minListCfg.strings;
                    AbstractMap<?, Double> maxList = maxListCfg.strings;
                    if (minList.isEmpty() && maxList.isEmpty()) {
                        result.append("<Empty>");
                        index = start + 2 + fullKey.length();
                        continue;
                    }
                    if (minList.size() == 1 && maxList.size() == 1 && minList.keySet().equals(maxList.keySet())) {
                        Map.Entry<?, Double> s = minList.entrySet().iterator().next();
                        if ("ability".equals(caseKey)) {
                            result.append(ModConfigs.ABILITIES.getAbilityById(s.getKey().toString()).map(Skill::getName).orElse(s.getKey().toString()));
                        } else if ("effect".equals(caseKey)) {
                            var effect = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.tryParse(s.getKey().toString()));
                            if (effect != null) {
                                result.append(I18n.get(effect.getDescriptionId()));
                            }
                        }
                        index = start + 2 + fullKey.length();
                        continue;
                    }
                    var total = minList.values().stream().reduce(0.0, Double::sum);
                    sb.append("<gray><<yellow>");
                    boolean first = true;
                    for (Map.Entry<?, Double> s : minList.entrySet()) {
                        if (!first) {sb.append("<gray> | <yellow>");} first = false;
                        String formattedWeight = DECIMAL_FORMAT.format(s.getValue() / total * 100);

                        if ("ability".equals(caseKey)) {
                            sb.append(ModConfigs.ABILITIES.getAbilityById(s.getKey().toString()).map(Skill::getName).orElse(s.getKey().toString()));
                        } else if ("effect".equals(caseKey)) {
                            var effect = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.tryParse(s.getKey().toString()));
                            if (effect != null) {
                                sb.append(I18n.get(effect.getDescriptionId()));
                            }
                        }

                        sb.append(" <darkgray>").append(formattedWeight).append("%<yellow>");
                    }
                    sb.append("<gray>><yellow>");
                    result.append(sb);
                    index = start + 2 + fullKey.length();
                    continue;
                }

                if (minObj != null && maxObj != null) {
                    String minStringValue = minObj.toString();
                    String maxStringValue = maxObj.toString();
                    if ("ability".equals(caseKey)) {
                        minStringValue = ModConfigs.ABILITIES.getAbilityById(minStringValue).map(Skill::getName).orElse(minStringValue);
                        maxStringValue = ModConfigs.ABILITIES.getAbilityById(maxStringValue).map(Skill::getName).orElse(maxStringValue);
                    } else if ("effect".equals(caseKey)) {
                        var minEffect = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.tryParse(minStringValue));
                        var maxEffect = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.tryParse(maxStringValue));
                        if (minEffect != null) {
                            minStringValue = I18n.get(minEffect.getDescriptionId());
                        }
                        if (maxEffect != null) {
                            maxStringValue = I18n.get(maxEffect.getDescriptionId());
                        }
                    }

                    if (minStringValue.equals(maxStringValue)) {
                        result.append(minStringValue);
                    } else {
                        result.append(minStringValue).append("-").append(maxStringValue);
                    }
                    index = start + 2 + fullKey.length();
                    continue;
                }
            } else if (nextChar == '.') {
                int end = start + 2;

                while (end < description.length() && Character.isDigit(description.charAt(end))) {
                    end++;
                }

                boolean isPercentage = description.startsWith("fp", end);
                boolean isFloat = description.charAt(end) == 'f';
                if (isFloat || isPercentage) {
                    int decimals = Integer.parseInt(description.substring(start + 2, end));
                    int keyStart = end + (isPercentage ? 2 : 1);
                    String key = extractKey(description, keyStart);
                    Object minObj = minValues.get(key);
                    Object maxObj = maxValues.get(key);
                    if (minObj instanceof Number minNumber && maxObj instanceof Number maxNumber) {
                        double minNumberValue = isFloat ? (double)minNumber.floatValue() : minNumber.doubleValue();
                        double maxNumberValue = isFloat ? (double)maxNumber.floatValue() : maxNumber.doubleValue();
                        if (isPercentage) {
                            minNumberValue *= 100.0F;
                            maxNumberValue *= 100.0F;
                        }

                        if (minNumberValue == maxNumberValue) {
                            result.append(String.format("%." + decimals + "f", minNumberValue));
                        } else {
                            result.append(String.format("%." + decimals + "f", minNumberValue)).append("-")
                                .append(String.format("%." + decimals + "f", maxNumberValue));
                        }
                        formattedMinNumbers.put(key, minNumberValue);
                        formattedMaxNumbers.put(key, maxNumberValue);
                        index = keyStart + key.length();
                        continue;
                    }
                }
            } else if (nextChar == 't') {
                String key = extractKey(description, start + 2);
                Object minObj = minValues.get(key);
                Object maxObj = maxValues.get(key);
                if (minObj instanceof Number minNumber && maxObj instanceof Number maxNumber) {
                    int minSeconds = minNumber.intValue() / 20;
                    int maxSeconds = maxNumber.intValue() / 20;
                    if (minSeconds == maxSeconds) {
                        result.append(minSeconds);
                    } else {
                        result.append(minSeconds).append("-").append(maxSeconds);
                    }
                    formattedMinNumbers.put(key, (double)minSeconds);
                    formattedMaxNumbers.put(key, (double)maxSeconds);
                    index = start + 2 + key.length();
                    continue;
                }
            }

            result.append('%');
            index = start + 1;
        }

        String formatted = result.toString();
        return applyConditionals(formatted, formattedMinNumbers, formattedMaxNumbers);
    }

    private static String applyConditionals(String input, Map<String, Double> minValues, Map<String, Double> maxValues) {
        StringBuilder result = new StringBuilder();

        int index = 0;

        while (index < input.length()) {
            int start = input.indexOf(123, index);
            if (start == -1) {
                result.append(input.substring(index));
                break;
            }

            result.append(input, index, start);
            int end = input.indexOf(125, start);
            if (end == -1) {
                result.append(input.substring(start));
                break;
            }

            String expr = input.substring(start + 1, end);
            result.append(evaluateConditional(expr, minValues, maxValues));
            index = end + 1;
        }

        return result.toString();
    }

    /**
     * This is used for pluralization
     */
    private static String evaluateConditional(String expr, Map<String, Double> minValues, Map<String, Double> maxValues) {
        for(String part : expr.split("\\|")) {
            Matcher matcher = CONDITIONAL_PATTERN.matcher(part.trim());
            if (matcher.matches()) {
                String key = matcher.group(1);
                String operator = matcher.group(2);
                double compareValue = Double.parseDouble(matcher.group(3));
                String append = matcher.group(4);
                Double currentMin = minValues.get(key);
                Double currentMax = maxValues.get(key);
                if (currentMin != null && compare(currentMin, operator, compareValue) || currentMax != null && compare(currentMax, operator, compareValue)) {
                    return append;
                }
            }
        }

        return "";
    }

    private static boolean compare(double value, String operator, double compareValue) {
        return switch (operator) {
            case ">" -> value > compareValue;
            case "<" -> value < compareValue;
            case ">=" -> value >= compareValue;
            case "<=" -> value <= compareValue;
            case "=" -> value == compareValue;
            case "!=" -> value != compareValue;
            default -> false;
        };
    }

    private static String extractKey(String str, int start) {
        int end = start;

        while (end < str.length() && Character.isJavaIdentifierPart(str.charAt(end))) {
            end++;
        }


        return str.substring(start, end);
    }

    private static String extractKeyWithCase(String str, int start) {
        int end = start;

        while (end < str.length() && (Character.isJavaIdentifierPart(str.charAt(end)) || str.charAt(end) == '/')) {
            end++;
        }

        return str.substring(start, end);
    }
}
