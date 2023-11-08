package com.libre.video.toolkit;

import com.libre.spider.CssQuery;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.units.qual.A;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class RegexUtil {

	private final static Pattern M3U8_URL_PATTERN = Pattern.compile(
			"(https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#,?&*//=]*)(.m3u8)\\b([-a-zA-Z0-9@:%_\\+.~#,?&//=]*))",
			Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

	public static String matchM3u8Url(String value) {
		return getRegexValue(M3U8_URL_PATTERN, 0, value);
	}

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

	public static String getRegexValue(Pattern pattern, int regexGroup, String value) {
		// 处理正则表达式
		Matcher matcher = pattern.matcher(value);
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
