package com.ssafy.yamyam.domain.video.service;

import com.ssafy.yamyam.domain.video.mapper.VideoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.*;
import java.nio.file.*;
import java.util.UUID;

@Slf4j
@Service
@Profile("prod")
@RequiredArgsConstructor
public class HlsTranscoderService {

    private final S3Client s3Client;
    private final S3VideoStorage s3VideoStorage;
    private final VideoMapper videoMapper;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Async
    public void transcodeAsync(Long videoId, String s3Key) {
        Path tmpDir = null;
        try {
            // 1. Download original from S3 to temp file
            tmpDir = Files.createTempDirectory("hls-" + UUID.randomUUID());
            Path inputFile = tmpDir.resolve("input.mp4");

            s3Client.getObject(
                r -> r.bucket(bucket).key(s3Key),
                inputFile
            );

            // 2. Run FFmpeg to generate HLS
            Path m3u8 = tmpDir.resolve("playlist.m3u8");
            Path segmentPattern = tmpDir.resolve("seg_%03d.ts");

            boolean success = runFfmpeg(
                inputFile.toString(),
                segmentPattern.toString(),
                m3u8.toString()
            );

            if (!success) {
                log.warn("[HLS] FFmpeg 변환 실패 — videoId={}, 원본 URL 유지", videoId);
                return;
            }

            // 3. Upload .m3u8 and .ts segments to S3 under hls/{videoId}/
            String hlsPrefix = "hls/" + videoId + "/";

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(tmpDir)) {
                for (Path file : stream) {
                    String filename = file.getFileName().toString();
                    if (!filename.endsWith(".m3u8") && !filename.endsWith(".ts")) continue;

                    String hlsKey = hlsPrefix + filename;
                    String contentType = filename.endsWith(".m3u8")
                        ? "application/vnd.apple.mpegurl"
                        : "video/MP2T";

                    s3Client.putObject(
                        PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(hlsKey)
                            .contentType(contentType)
                            .build(),
                        file
                    );
                }
            }

            // 4. Update DB video_url to HLS playlist key
            String hlsKey = hlsPrefix + "playlist.m3u8";
            videoMapper.updateVideoUrl(videoId, hlsKey);
            log.info("[HLS] 변환 완료 — videoId={}, hlsKey={}", videoId, hlsKey);

        } catch (Exception e) {
            log.error("[HLS] 변환 중 오류 — videoId={}: {}", videoId, e.getMessage(), e);
        } finally {
            if (tmpDir != null) deleteTempDir(tmpDir);
        }
    }

    private boolean runFfmpeg(String inputPath, String segmentPattern, String m3u8Path) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-y",
                "-i", inputPath,
                "-c:v", "libx264",
                "-c:a", "aac",
                "-hls_time", "2",
                "-hls_list_size", "0",
                "-hls_segment_filename", segmentPattern,
                "-f", "hls",
                m3u8Path
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // drain stdout/stderr to prevent blocking
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                br.lines().forEach(line -> log.debug("[FFmpeg] {}", line));
            }

            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException e) {
            log.warn("[HLS] ffmpeg 명령어를 찾을 수 없음 (미설치) — {}", e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void deleteTempDir(Path dir) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path file : stream) Files.deleteIfExists(file);
        } catch (IOException ignored) {}
        try { Files.deleteIfExists(dir); } catch (IOException ignored) {}
    }
}
