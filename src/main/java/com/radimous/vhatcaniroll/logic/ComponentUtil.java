package com.radimous.vhatcaniroll.logic;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComponentUtil {

    private static final Pattern MATCHING_VALUES = Pattern.compile("^.*(.+)-(\\1) .*$");

    public static Component replace(Component component, String target, TextComponent replacement) {
        if (!(component instanceof TextComponent base)) {return component;} else {
            List<Component> siblings = base.getSiblings();
            siblings.add(0, base.plainCopy().setStyle(base.getStyle()));
            for (int result = 0; result < siblings.size(); result++) {
                Component sibling = siblings.get(result);
                if (sibling instanceof TextComponent) {
                    String text = ((TextComponent) sibling).getText();
                    if (!text.isEmpty()) {
                        List<Component> parts = new ArrayList<>();
                        Style styledReplacement = replacement.getStyle() == Style.EMPTY ? sibling.getStyle() : Style.EMPTY;
                        if (text.equals(target)) {parts.add(replacement.copy().withStyle(styledReplacement));} else {
                            for (String raw : text.split(Pattern.quote(target))) {
                                parts.add(new TextComponent(raw).setStyle(sibling.getStyle()));
                                parts.add(replacement.copy().withStyle(styledReplacement));
                            }
                            parts.remove(parts.size() - 1);
                        }
                        siblings.remove(result);
                        for (int j = 0; j < parts.size(); j++) {siblings.add(result, parts.get(parts.size() - j - 1));}
                    }
                }
            }
            TextComponent result = new TextComponent("");
            result.setStyle(base.getStyle());
            for (Component sibling : siblings) {result.append(sibling);}

            return result;
        }
    }

    public static MutableComponent simplifyMatchingValues(MutableComponent res) {
        Matcher matcher = MATCHING_VALUES.matcher(res.getString());
        if (matcher.matches()) {
            String number = matcher.group(1);
            res = (MutableComponent) replace(res, number + "-" + number, (TextComponent) new TextComponent(number).withStyle(res.getStyle()));
        }
        return res;
    }
}
