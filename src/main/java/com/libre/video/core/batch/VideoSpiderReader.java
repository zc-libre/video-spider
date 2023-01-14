package com.libre.video.core.batch;

import com.libre.video.pojo.Video;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.IteratorItemReader;

import java.util.List;

/**
 * @author: Libre
 * @Date: 2023/1/13 11:03 PM
 */

public interface VideoSpiderReader extends ItemReader<List<Video>> {



}
