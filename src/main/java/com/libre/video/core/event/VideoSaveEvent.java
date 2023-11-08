package com.libre.video.core.event;

import com.libre.video.pojo.Video;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoSaveEvent {

	private List<Video> videoList;

}
