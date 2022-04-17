package com.libre.video.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VideoType {
    /**
     *  91原创
     */
    ORI(1, "ori"),

    LONGER(2, "longer"),

    LONG(3, "long"),

    TOP(4, "top"),

    TF(5, "tf"),

    MF(6, "mf"),

    RF(7, "rf"),

    HOT(8, "hot"),

    MD(9, "md"),

   // HD(10, "hd"),
    ;


    private final int type;

    private final String name;

}
