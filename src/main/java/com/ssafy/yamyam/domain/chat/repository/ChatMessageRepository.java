package com.ssafy.yamyam.domain.chat.repository;

import com.ssafy.yamyam.domain.chat.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findTop60ByTeamIdOrderByTimestampAsc(Long teamId);
}
