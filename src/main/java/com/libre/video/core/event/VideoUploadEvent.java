package com.libre.video.core.event;

import com.libre.video.pojo.Video;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.io.Resource;

/**
 * @author: Libre
 * @Date: 2022/5/15 7:22 AM
 */
@Data
@AllArgsConstructor
public class VideoUploadEvent {

	private Boolean end;

	private Video video;

	private Resource resource;
}
