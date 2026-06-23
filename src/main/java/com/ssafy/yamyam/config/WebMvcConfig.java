package com.ssafy.yamyam.config;

import com.ssafy.yamyam.global.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${yamyam.video.upload-dir}")
    private String uploadDir;

    private final JwtInterceptor jwtInterceptor;

    public WebMvcConfig(JwtInterceptor jwtInterceptor) {
        this.jwtInterceptor = jwtInterceptor;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /videos/** -> classpath 더미 영상 OR 유저 업로드 영상 (순서대로 탐색)
        registry.addResourceHandler("/videos/**")
                .addResourceLocations(
                        "classpath:/static/videos/",
                        "file:" + uploadDir + "/"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    // 인터셉터 적용 경로 설정 (새로 추가!)
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**", "/graphql")
                .excludePathPatterns("/api/users/login", "/api/users/signup", "/api/teams/invite/**",
                        "/api/videos/*/analyze");
    }
}
