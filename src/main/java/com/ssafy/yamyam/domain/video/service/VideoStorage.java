package com.ssafy.yamyam.domain.video.service;

import org.springframework.web.multipart.MultipartFile;

public interface VideoStorage {
    /** 파일을 저장하고 DB에 넣을 식별값을 반환 (로컬: /videos/xxx.mp4, S3: videos/uuid.mp4) */
    String save(MultipartFile file);

    /** DB 식별값 → 실제 접근 URL (로컬: 그대로, S3: presigned URL) */
    String toUrl(String stored);

    /** 저장된 파일 삭제 */
    void delete(String stored);
}
