package com.libre.video.core.event;

import com.libre.video.pojo.BaAvVideo;
import com.libre.video.pojo.Video;
import lombok.*;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class BaAvVideoSaveEvent {

    private List<BaAvVideo> videoList;

}
