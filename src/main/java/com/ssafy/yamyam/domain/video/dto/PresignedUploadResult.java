package com.ssafy.yamyam.domain.video.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PresignedUploadResult {
    private final String uploadUrl;
    private final String key;
}
