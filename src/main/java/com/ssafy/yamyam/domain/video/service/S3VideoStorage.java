package com.ssafy.yamyam.domain.video.service;

import com.ssafy.yamyam.domain.video.dto.PresignedUploadResult;
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
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

@Component
@Profile("prod")
@RequiredArgsConstructor
public class S3VideoStorage implements VideoStorage {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private static final Duration DOWNLOAD_URL_TTL = Duration.ofHours(1);
    private static final Duration DOWNLOAD_URL_REFRESH_MARGIN = Duration.ofMinutes(5);

    private final Map<String, CachedUrl> downloadUrlCache = new ConcurrentHashMap<>();

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
                            .cacheControl("public, max-age=2592000")
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

        CachedUrl cached = downloadUrlCache.get(stored);
        if (cached != null && cached.expiresAt().isAfter(Instant.now().plus(DOWNLOAD_URL_REFRESH_MARGIN))) {
            return cached.url();
        }

        // S3 key -> presigned URL (1시간 유효). 같은 key는 만료 직전까지 재사용해서
        // 프론트 폴링 중 video src가 계속 바뀌지 않게 한다.
        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(r ->
                r.signatureDuration(DOWNLOAD_URL_TTL)
                        .getObjectRequest(g -> g.bucket(bucket).key(stored))
        );
        String url = presigned.url().toString();
        downloadUrlCache.put(stored, new CachedUrl(url, Instant.now().plus(DOWNLOAD_URL_TTL)));
        return url;
    }

    @Override
    public void delete(String stored) {
        if (stored == null || stored.startsWith("/") || stored.startsWith("http")) return;
        s3Client.deleteObject(r -> r.bucket(bucket).key(stored));
    }

    @Override
    public PresignedUploadResult presignPut(String contentType) {
        String ext = extFromContentType(contentType);
        String key = "videos/" + UUID.randomUUID() + ext;

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(r ->
                r.signatureDuration(Duration.ofMinutes(15))
                 .putObjectRequest(p -> p.bucket(bucket).key(key).contentType(contentType))
        );
        return new PresignedUploadResult(presigned.url().toString(), key);
    }

    private String getExt(String filename) {
        if (filename == null || !filename.contains(".")) return ".mp4";
        return filename.substring(filename.lastIndexOf("."));
    }

    private String extFromContentType(String contentType) {
        if (contentType == null) return ".mp4";
        return switch (contentType) {
            case "video/quicktime" -> ".mov";
            case "video/webm" -> ".webm";
            case "video/ogg" -> ".ogv";
            default -> ".mp4";
        };
    }

    private record CachedUrl(String url, Instant expiresAt) {}
}
