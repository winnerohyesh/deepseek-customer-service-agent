package com.winnerohyesh.customeragent.service;

import com.winnerohyesh.customeragent.entity.ChatMessage;
import com.winnerohyesh.customeragent.entity.Conversation;
import com.winnerohyesh.customeragent.entity.KnowledgeChunk;
import com.winnerohyesh.customeragent.exception.ApiException;
import com.winnerohyesh.customeragent.repository.ChatMessageRepository;
import com.winnerohyesh.customeragent.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final DeepSeekService deepSeekService;
    private final KnowledgeService knowledgeService;

    @Transactional
    public String sendMessage(String conversationId, String userId, String message) {
        Conversation conversation = conversationRepository.findByConversationId(conversationId)
                .orElseThrow(() -> new ApiException("会话不存在: " + conversationId));

        if (!conversation.getUserId().equals(userId)) {
            throw new ApiException("该用户无权访问此会话");
        }

        ChatMessage userMessage = ChatMessage.builder()
                .conversationId(conversationId)
                .role("user")
                .content(message)
                .createdAt(Instant.now())
                .build();
        chatMessageRepository.save(userMessage);

        List<ChatMessage> history = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        List<KnowledgeChunk> relevantKnowledge = knowledgeService.searchRelevantKnowledge(message, 5);

        String answer = deepSeekService.generateReply(history, relevantKnowledge, message);

        ChatMessage assistantMessage = ChatMessage.builder()
                .conversationId(conversationId)
                .role("assistant")
                .content(answer)
                .createdAt(Instant.now())
                .build();
        chatMessageRepository.save(assistantMessage);

        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);

        return answer;
    }

    public List<ChatMessage> getConversationHistory(String conversationId) {
        return chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }
}
