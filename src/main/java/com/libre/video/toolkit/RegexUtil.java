package com.libre.video.toolkit;

import com.libre.spider.CssQuery;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.units.qual.A;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class RegexUtil {

    public static String getRegexValue(String regex, int regexGroup, String value) {
        // 处理正则表达式
        Matcher matcher = Pattern.compile(regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(value);
        if (!matcher.find()) {
            return null;
        }
        // 正则 group
        if (regexGroup > 0) {
            return matcher.group(regexGroup);
        }
        return matcher.group();
    }


}
