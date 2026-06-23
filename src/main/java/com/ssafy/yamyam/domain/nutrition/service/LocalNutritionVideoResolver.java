package com.ssafy.yamyam.domain.nutrition.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
@Profile("!prod")
public class LocalNutritionVideoResolver implements NutritionVideoResolver {

    @Value("${yamyam.video.upload-dir}")
    private String uploadDir;

    @Override
    public File resolveVideoFile(Long videoId, String storedVideoPath) {
        log.info("[영양분석-Local] 로컬 디스크에서 비디오 파일 분석 준비: {}", storedVideoPath);

        // 1. 이미 완전한 절대 경로(C:\... 또는 /User/...) 형태로 넘어온 경우, 그대로 File 객체로 만들어 반환합니다.
        File directFile = new File(storedVideoPath);
        if (directFile.isAbsolute() && directFile.exists()) {
            log.info("[영양분석-Local] 인입된 경로가 절대 경로이며 파일이 존재하여 그대로 사용합니다.");
            return directFile;
        }

        // 2. 만약 경로가 절대 경로인데 파일 시스템에 존재하지 않거나, 파일명만 추출해야 하는 경우
        String filename = storedVideoPath;
        
        // 윈도우 역슬래시(\\)나 리눅스 슬래시(/)의 마지막 인덱스를 찾아 파일명만 분리
        if (filename.contains("\\")) {
            filename = filename.substring(filename.lastIndexOf("\\") + 1);
        } else if (filename.contains("/")) {
            filename = filename.substring(filename.lastIndexOf("/") + 1);
        }

        File folder = new File(uploadDir).isAbsolute()
                ? new File(uploadDir)
                : new File(System.getProperty("user.dir"), uploadDir);

        return new File(folder, filename);
    }
}