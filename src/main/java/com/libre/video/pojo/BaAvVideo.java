package com.libre.video.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.libre.core.time.DatePattern;
import com.libre.core.toolkit.StringUtil;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Objects;

/**
 * @author: Libre
 * @Date: 2022/5/12 2:35 AM
 */
@Data
@TableName("ba_av_video")
public class BaAvVideo  {

	@TableId(type = IdType.INPUT)
	private Long id;

	private String url;

	private String realUrl;

	private String title;

	private String image;

	private String duration;

	private String author;

	private Integer lookNum;

	private Integer collectNum;
	private String publishTime;

	@TableField(exist = false)
	private LocalDate time;


	@TableField(fill = FieldFill.INSERT_UPDATE)
	@JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
	private LocalDateTime createTime;


	@TableField(fill = FieldFill.UPDATE)
	private LocalDateTime updateTime;


	public LocalDate getTime() {
		if (StringUtil.isBlank(this.publishTime)) {
			return null;
		}
		if (DatePattern.NORM_DATE_PATTERN.length() == this.publishTime.length()) {
			return LocalDate.parse(this.publishTime, DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN));
		} else if (6 == this.publishTime.length()) {
			String format = LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL));
			String timeStr = format.substring(0, 5) + this.publishTime;
			return LocalDate.parse(timeStr, DateTimeFormatter.ofPattern(DatePattern.CHINESE_DATE_PATTERN));
		}
		return null;
	}
}
