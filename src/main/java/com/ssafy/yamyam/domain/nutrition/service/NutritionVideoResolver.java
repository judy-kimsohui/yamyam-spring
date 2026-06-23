package com.ssafy.yamyam.domain.nutrition.service;

import java.io.File;

/**
 * 영양 분석 도메인에서 분석용 비디오 파일(File)을 물리적으로 확보(Resolve)하는 인터페이스
 */
public interface NutritionVideoResolver {
    /**
     * 환경별 스토리지 매커니즘을 통해 비디오 파일을 찾아 Local File 객체로 반환합니다.
     */
    File resolveVideoFile(Long videoId, String storedVideoPath);
}