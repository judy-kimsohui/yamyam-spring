package com.ssafy.yamyam.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${yamyam.video.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /videos/** → classpath 더미 영상 OR 유저 업로드 영상 (순서대로 탐색)
        registry.addResourceHandler("/videos/**")
                .addResourceLocations(
                        "classpath:/static/videos/",
                        "file:" + uploadDir + "/"
                );
    }
}
