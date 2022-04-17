package com.libre.video.mapper;

import com.libre.video.pojo.Video91;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface Video9sEsRepository extends CrudRepository<Video91, Long> {


    List<Video91> findVideosByTitle(String title);
}
