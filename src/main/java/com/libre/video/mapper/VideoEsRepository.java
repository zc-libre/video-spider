package com.libre.video.mapper;

import com.libre.video.pojo.Video;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.Nullable;

import java.util.List;

public interface VideoEsRepository extends CrudRepository<Video, Long> {


	List<Video> findAll();

    List<Video> findVideosByTitle(String title);

	List<Video> findAllByTitleLike(String title);
}
