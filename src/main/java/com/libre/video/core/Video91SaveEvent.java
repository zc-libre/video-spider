package com.libre.video.core;

import com.libre.video.pojo.Video91;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Video91SaveEvent {

    private List<Video91> video91List;
}
