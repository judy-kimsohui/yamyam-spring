package com.ssafy.yamyam.domain.nutrition.service;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class VideoFrameExtractor {

    @Value("${yamyam.video.upload-dir}")
    private String uploadDir;

    // ✅ GMS 게이트웨이 바디 크기 제한 회피용 — 프레임당 목표 최대 용량
    private static final int TARGET_MAX_BYTES = 25 * 1024; // 25KB

    public List<byte[]> extractFrames(String stored) throws IOException {
        File videoFile;

        // ⭕ [정밀 조율] S3에서 받아온 완전한 로컬 절대 경로가 들어온 경우, 복잡한 리졸브를 패스하고 직접 매핑
        if (stored.startsWith("/") && stored.contains("_video_")) {
            videoFile = new File(stored);
        } else {
            // 기존 로컬 파일 검증 로직 하위 호환성 유지
            String filename = stored.replaceFirst("^/videos/", "");
            videoFile = resolveVideoFile(filename);
        }

        if (!videoFile.exists()) {
            throw new IOException("영상 파일을 찾을 수 없습니다: " + videoFile.getAbsolutePath());
        }

        List<byte[]> frames = new ArrayList<>();

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoFile.getAbsolutePath())) {
            grabber.start();

            log.info("[프레임 추출] codec={} pixelFormat={} width={} height={} lengthInTime={} frameRate={}",
                    grabber.getVideoCodecName(), grabber.getPixelFormat(),
                    grabber.getImageWidth(), grabber.getImageHeight(),
                    grabber.getLengthInTime(), grabber.getFrameRate());

            double duration = grabber.getLengthInTime() / 1000000.0;
            if (duration <= 0) duration = 10.0;

            List<Long> targetTimestamps = List.of(
                    (long) ((duration / 3.0) * 1000000),
                    (long) ((duration * 2.0 / 3.0) * 1000000)
            );

            Java2DFrameConverter converter = new Java2DFrameConverter();

            for (Long timestamp : targetTimestamps) {
                grabber.setTimestamp(timestamp);

                Frame frame = null;
                for (int retry = 0; retry < 10; retry++) {
                    frame = grabber.grabImage();
                    if (frame != null && frame.image != null) {
                        break;
                    }
                }

                if (frame != null && frame.image != null) {
                    BufferedImage bufferedImage = converter.convert(frame);
                    if (bufferedImage != null) {
                        BufferedImage resizedImage = resizeImage(bufferedImage, 512);

                        byte[] imageBytes = compressToTargetSize(resizedImage);

                        if (ImageIO.read(new java.io.ByteArrayInputStream(imageBytes)) == null) {
                            log.warn("[프레임 추출] 생성된 JPEG가 유효하지 않아 건너뜀 (size={} bytes)", imageBytes.length);
                            continue;
                        }

                        log.info("[프레임 추출 성공] 압축 완료된 이미지 용량: {} KB", imageBytes.length / 1024);
                        frames.add(imageBytes);
                    }
                }
            }
            grabber.stop();
        } catch (Exception e) {
            log.error("JavaCV 프레임 추출 중 최종 예외 발생: {}", e.getMessage(), e);
            throw new IOException("비디오 디코딩 처리 실패: " + e.getMessage(), e);
        }

        return frames;
    }

    // ✅ 추가: 목표 용량(TARGET_MAX_BYTES) 이하가 될 때까지 품질을 낮춰가며 재압축.
    //    화면이 복잡한(다운로드한) 영상이라도 항상 일정 크기 이하로 보장 → GMS 바디 크기 제한 회피
    private byte[] compressToTargetSize(BufferedImage image) throws IOException {
        float quality = 0.6f;
        byte[] result = encodeJpeg(image, quality);

        while (result.length > TARGET_MAX_BYTES && quality > 0.15f) {
            quality -= 0.1f;
            result = encodeJpeg(image, quality);
            log.debug("프레임 재압축: quality={} size={}KB", quality, result.length / 1024);
        }

        // 그래도 너무 크면 해상도를 한 단계 더 축소해서 마지막으로 한 번 더 압축
        if (result.length > TARGET_MAX_BYTES) {
            BufferedImage smaller = resizeImage(image, 384);
            result = encodeJpeg(smaller, 0.5f);
        }

        return result;
    }

    private byte[] encodeJpeg(BufferedImage image, float quality) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {

            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }

            writer.write(null, new IIOImage(image, null, null), param);
            writer.dispose();
            ios.flush();

            return baos.toByteArray();
        }
    }

    private File resolveVideoFile(String filename) {
        File dir = new File(uploadDir).isAbsolute()
                ? new File(uploadDir)
                : new File(System.getProperty("user.dir"), uploadDir);
        return new File(dir, filename);
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth) {
        double aspectRatio = (double) originalImage.getHeight() / originalImage.getWidth();
        int targetHeight = (int) (targetWidth * aspectRatio);

        java.awt.Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, java.awt.Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(resultingImage, 0, 0, null);
        g2d.dispose();

        return outputImage;
    }
}