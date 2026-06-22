package com.ssafy.yamyam.domain.video.controller;

import com.ssafy.yamyam.domain.video.dto.VideoDto;
import com.ssafy.yamyam.domain.video.service.VideoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class VideoGraphQLController {

    private final VideoService videoService;

    @QueryMapping
    public List<VideoDto> videos(
            @Argument Long teamId,
            @Argument Long userId,
            @Argument String date) {

        Long loginUserId = getLoginUserId();
        String mealDate = (date != null && !date.isBlank()) ? date : LocalDate.now().toString();

        if (teamId != null) {
            return videoService.getTeamVideos(teamId, mealDate, loginUserId);
        } else if (userId != null) {
            return videoService.getMyVideos(userId, mealDate, loginUserId);
        }
        return List.of();
    }

    private Long getLoginUserId() {
        try {
            ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attrs.getRequest();
            return (Long) request.getAttribute("loginUserKey");
        } catch (Exception e) {
            return null;
        }
    }
}
