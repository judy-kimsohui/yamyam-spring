package com.ssafy.yamyam.domain.nutrition.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.File;

@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class S3NutritionVideoResolver implements NutritionVideoResolver {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public File resolveVideoFile(Long videoId, String storedVideoPath) {
        log.info("[영양분석-Prod] S3로부터 분석용 임시 비디오 다운로드 시작 (Bucket: {}, Key: {})", bucket, storedVideoPath);

        try {
            File appDir = new File(System.getProperty("user.dir"));
            String tempFileName = "s3_video_" + videoId + "_" + System.currentTimeMillis() + ".mp4";
            File tempVideoFile = new File(appDir, tempFileName);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(storedVideoPath)
                    .build();

            s3Client.getObject(getObjectRequest, software.amazon.awssdk.core.sync.ResponseTransformer.toFile(tempVideoFile));
            
            return tempVideoFile;
        } catch (Exception e) {
            throw new RuntimeException("영양 분석용 S3 비디오 다운로드 실패: " + e.getMessage(), e);
        }
    }
}