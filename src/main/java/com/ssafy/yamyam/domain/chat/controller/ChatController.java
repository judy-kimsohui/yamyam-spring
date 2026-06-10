package com.ssafy.yamyam.domain.chat.controller;

import com.ssafy.yamyam.domain.chat.dto.ChatMessageDto;
import com.ssafy.yamyam.domain.chat.service.ChatService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messaging;

    // ── WebSocket: 메시지 수신 → MongoDB 저장 → 팀 전체에 브로드캐스트 ──
    @MessageMapping("/chat/{teamId}")
    public void handleMessage(
            @DestinationVariable Long teamId,
            @org.springframework.messaging.handler.annotation.Payload Map<String, Object> payload) {

        Long userId   = payload.get("userId")   != null ? Long.parseLong(payload.get("userId").toString())   : 0L;
        String nick   = payload.get("nickName") != null ? payload.get("nickName").toString() : "익명";
        String text   = payload.get("text")     != null ? payload.get("text").toString()     : "";

        if (text.isBlank()) return;

        ChatMessageDto saved = chatService.save(teamId, userId, nick, text);
        messaging.convertAndSend("/topic/chat/" + teamId, saved);
    }

    // ── REST: 최근 메시지 히스토리 조회 ──
    @GetMapping("/api/chat/{teamId}/messages")
    @ResponseBody
    public ResponseEntity<List<ChatMessageDto>> getHistory(@PathVariable Long teamId) {
        return ResponseEntity.ok(chatService.getHistory(teamId));
    }
}
