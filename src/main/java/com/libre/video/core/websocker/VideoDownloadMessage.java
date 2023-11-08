package com.libre.video.core.websocker;

import com.libre.video.pojo.Video;
import lombok.Data;

/**
 * @author: Libre
 * @Date: 2022/5/14 7:59 AM
 */
@Data
public class VideoDownloadMessage {

	private Long videoId;

	private String percentage;

	private Boolean end;

	private Integer type;

	private String message;

	private Video video;

}
