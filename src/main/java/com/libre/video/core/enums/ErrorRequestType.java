package com.libre.video.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorRequestType {

    REQUEST(0),
    REQUEST_CATEGORY(1),

    REQUEST_PAGE(2),

    PARSE(3)
    ;

    private final Integer code;
}
