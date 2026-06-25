package com.ssafy.yamyam.config;

import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfig {

    // application.properties에 설정된 SSAFY GMS 전용 api-key를 읽어옵니다.
    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    // application.properties에 설정된 SSAFY GMS 프록시 주소를 읽어옵니다.
    @Value("${spring.ai.openai.base-url:https://api.openai.com}")
    private String baseUrl;

    @Bean
    public OpenAiApi openAiApi() {
        // 스프링 AI 오토설정이 GMS 환경을 인지하지 못하므로 명시적으로 주소와 키를 바인딩하여 빈 등록
        return new OpenAiApi(baseUrl, apiKey);
    }
}