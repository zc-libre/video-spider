package com.libre.video;

import com.libre.video.core.request.strategy.Video9SRequestStrategy;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableEncryptableProperties
@SpringBootApplication
@RequiredArgsConstructor
@EnableAspectJAutoProxy(exposeProxy = true, proxyTargetClass = true)
public class VideoSpiderApplication implements ApplicationRunner {

    private final Video9SRequestStrategy video9sRequest;

    public static void main(String[] args) {
        SpringApplication.run(VideoSpiderApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
      //video9sRequest.execute();
    }
}
