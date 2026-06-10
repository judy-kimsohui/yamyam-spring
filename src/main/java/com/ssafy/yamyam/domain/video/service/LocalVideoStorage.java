package com.ssafy.yamyam.domain.video.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
@Profile("!prod")
public class LocalVideoStorage implements VideoStorage {

    @Value("${yamyam.video.upload-dir}")
    private String uploadDir;

    @Override
    public String save(MultipartFile file) {
        File folder = new File(uploadDir).isAbsolute()
                ? new File(uploadDir)
                : new File(System.getProperty("user.dir"), uploadDir);

        if (!folder.exists()) folder.mkdirs();

        String ext = getExt(file.getOriginalFilename());
        String filename = UUID.randomUUID() + ext;

        try {
            file.transferTo(new File(folder, filename));
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패: " + e.getMessage(), e);
        }
        return "/videos/" + filename;
    }

    @Override
    public String toUrl(String stored) {
        return stored;
    }

    private String getExt(String filename) {
        if (filename == null || !filename.contains(".")) return ".mp4";
        return filename.substring(filename.lastIndexOf("."));
    }
}
