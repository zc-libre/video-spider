package com.libre.video.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RequestTypeEnum {

	/**
	 * 91pron
	 */
	REQUEST_91(1, "https://91porn.com/v.php?next=watch"),

	/**
	 * 九色
	 */
	REQUEST_9S(2, "https://91porny.com/video/category/latest"),

	/**
	 * baav
	 */
	REQUEST_BA_AV(3, "https://www.tasexy.com");

	private final int type;

	private final String baseUrl;

	public static RequestTypeEnum find(int type) {
		for (RequestTypeEnum typeEnum : RequestTypeEnum.values()) {
			if (typeEnum.getType() == type) {
				return typeEnum;
			}
		}
		return null;
	}

}
