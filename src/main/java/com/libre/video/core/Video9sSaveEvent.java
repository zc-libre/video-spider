package com.libre.video.core;

import com.libre.video.pojo.dto.Video9s;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Video9sSaveEvent {

    private List<Video9s> videoList;
}
