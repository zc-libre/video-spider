package com.libre.video.core.request;

public interface VideoRequest {

    void execute(String url);


    Integer parsePageSize(String html);
}
