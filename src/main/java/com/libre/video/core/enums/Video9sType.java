package com.libre.video.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Video9sType {

    /**
     *  91原创
     */
    ORI(1, "ori"),

    LONGER_LIST(2, "longer-list"),

    LONG_LIST(3, "long-list"),

    TOP_LIST(4, "top-list"),

    TOP_FAVORITE(5, "top_favorite"),

    MOST_FAVORITE(6, "most-favorite"),

    RECENT_FAVORITE(7, "recent-favorite"),

    HOT_LIST(8, "hot-list"),

    MONTH_DISCUSS(9, "month-discuss"),

    LATEST(10, "latest")
    ;

    private final int type;

    private final String name;
}
