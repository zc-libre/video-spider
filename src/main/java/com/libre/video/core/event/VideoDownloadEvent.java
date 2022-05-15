package com.libre.video.core.event;

import com.libre.video.pojo.Video;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author: Libre
 * @Date: 2022/5/15 7:22 AM
 */
@Data
@AllArgsConstructor
public class VideoDownloadEvent  {

	private Boolean end;

	private Video video;
}
