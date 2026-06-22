package com.ssafy.yamyam.domain.video.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Component
@Profile("prod")
@RequiredArgsConstructor
public class S3VideoStorage implements VideoStorage {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public String save(MultipartFile file) {
        String key = "videos/" + UUID.randomUUID() + getExt(file.getOriginalFilename());
        String contentType = file.getContentType() != null ? file.getContentType() : "video/mp4";

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 실패: " + e.getMessage(), e);
        }
        return key;
    }

    @Override
    public String toUrl(String stored) {
        if (stored == null) return null;
        // 기존 로컬 경로(/videos/...)나 이미 완전한 URL은 그대로 반환
        if (stored.startsWith("/") || stored.startsWith("http")) return stored;

        // S3 key -> presigned URL (1시간 유효)
        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(r ->
                r.signatureDuration(Duration.ofHours(1))
                        .getObjectRequest(g -> g.bucket(bucket).key(stored))
        );
        return presigned.url().toString();
    }

    @Override
    public void delete(String stored) {
        if (stored == null || stored.startsWith("/") || stored.startsWith("http")) return;
        s3Client.deleteObject(r -> r.bucket(bucket).key(stored));
    }

    private String getExt(String filename) {
        if (filename == null || !filename.contains(".")) return ".mp4";
        return filename.substring(filename.lastIndexOf("."));
    }
}
