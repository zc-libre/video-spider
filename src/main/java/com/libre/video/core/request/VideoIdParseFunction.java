package com.libre.video.core.request;

import com.libre.core.function.CheckedFunction;

/**
 * @author: Libre
 * @Date: 2022/5/12 2:59 AM
 */
@FunctionalInterface
public interface VideoIdParseFunction extends CheckedFunction<String, Integer> {

}
