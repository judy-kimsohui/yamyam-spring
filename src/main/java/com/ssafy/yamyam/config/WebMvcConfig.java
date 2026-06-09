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
        // /videos/** → classpath 더미 영상 OR 유저 업로드 영상 (순서대로 탐색)
        registry.addResourceHandler("/videos/**")
                .addResourceLocations(
                        "classpath:/static/videos/",
                        "file:" + uploadDir + "/"
                );
    }

    // CORS 전역 설정 (기존 코드)
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*");
    }

    // 인터셉터 적용 경로 설정 (새로 추가!)
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**") // /api/로 시작하는 모든 요청은 토큰 검사를 거치게 함
                .excludePathPatterns("/api/users/login", "/api/users/signup"); // 💡 단, 로그인과 회원가입은 토큰이 없으므로 제외!
    }
}
