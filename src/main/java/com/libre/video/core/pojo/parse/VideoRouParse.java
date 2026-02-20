package com.libre.video.core.pojo.parse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * 肉视频列表页解析模型（__NEXT_DATA__ JSON）
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoRouParse implements VideoParse {

	private String id;

	private String vid;

	private String name;

	private String nameZh;

	private List<String> tags;

	private Double duration;

	private Integer viewCount;

	private String coverImageUrl;

	private List<VideoSource> sources;

	private String createdAt;

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class VideoSource {

		private String id;

		private String videoId;

		private Integer resolution;

		private String folder;

	}

}
