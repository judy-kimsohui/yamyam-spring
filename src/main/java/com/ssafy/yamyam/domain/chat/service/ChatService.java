package com.ssafy.yamyam.domain.chat.service;

import com.ssafy.yamyam.domain.chat.dto.ChatMessageDto;
import com.ssafy.yamyam.domain.chat.model.ChatMessage;
import com.ssafy.yamyam.domain.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository repo;

    public ChatMessageDto save(Long teamId, Long userId, String nickName, String text) {
        ChatMessage msg = new ChatMessage();
        msg.setTeamId(teamId);
        msg.setUserId(userId);
        msg.setNickName(nickName);
        msg.setText(text);
        msg.setTimestamp(Instant.now());
        repo.save(msg);
        return toDto(msg);
    }

    public List<ChatMessageDto> getHistory(Long teamId) {
        return repo.findTop60ByTeamIdOrderByTimestampAsc(teamId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    private ChatMessageDto toDto(ChatMessage m) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(m.getId());
        dto.setTeamId(m.getTeamId());
        dto.setUserId(m.getUserId());
        dto.setNickName(m.getNickName());
        dto.setText(m.getText());
        dto.setTimestamp(m.getTimestamp().toString());
        dto.setMine(false);
        return dto;
    }
}
