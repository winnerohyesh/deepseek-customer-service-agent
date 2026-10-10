package com.winnerohyesh.customeragent.service;

import com.winnerohyesh.customeragent.entity.Conversation;
import com.winnerohyesh.customeragent.entity.ConversationStatus;
import com.winnerohyesh.customeragent.repository.ConversationRepository;
import com.winnerohyesh.customeragent.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final ConversationRepository conversationRepository;

    public Conversation createSession(String userId, String title) {
        String conversationId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        Conversation conversation = Conversation.builder()
                .conversationId(conversationId)
                .userId(userId)
                .title(title == null || title.isBlank() ? "新会话" : title)
                .status(ConversationStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return conversationRepository.save(conversation);
    }

    public Optional<Conversation> getByConversationId(String conversationId) {
        return conversationRepository.findByConversationId(conversationId);
    }

    public List<Conversation> listByUser(String userId) {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    public void closeSession(String conversationId) {
        Conversation conversation = conversationRepository.findByConversationId(conversationId)
                .orElseThrow(() -> new ApiException("会话不存在: " + conversationId));
        conversation.setStatus(ConversationStatus.CLOSED);
        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);
    }
}
