package com.winnerohyesh.customeragent.controller;

import com.winnerohyesh.customeragent.dto.ChatRequest;
import com.winnerohyesh.customeragent.dto.ChatResponse;
import com.winnerohyesh.customeragent.dto.ConversationCreateRequest;
import com.winnerohyesh.customeragent.entity.ChatMessage;
import com.winnerohyesh.customeragent.entity.Conversation;
import com.winnerohyesh.customeragent.service.ChatService;
import com.winnerohyesh.customeragent.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "chat", description = "会话与聊天接口")
public class ChatController {

    private final ChatService chatService;
    private final SessionService sessionService;

    @PostMapping("/sessions")
    @Operation(summary = "创建会话")
    public ResponseEntity<Map<String, Object>> createSession(@Valid @RequestBody ConversationCreateRequest request) {
        Conversation conversation = sessionService.createSession(request.getUserId(), request.getTitle());
        return ResponseEntity.ok(Map.of(
                "conversationId", conversation.getConversationId(),
                "title", conversation.getTitle(),
                "userId", conversation.getUserId()
        ));
    }

    @GetMapping("/sessions/{userId}")
    @Operation(summary = "查询用户会话列表")
    public ResponseEntity<List<Conversation>> listSessions(@PathVariable String userId) {
        return ResponseEntity.ok(sessionService.listByUser(userId));
    }

    @PostMapping("/chat")
    @Operation(summary = "发送聊天消息")
    public ResponseEntity<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request) {
        String answer = chatService.sendMessage(request.getConversationId(), request.getUserId(), request.getMessage());
        return ResponseEntity.ok(ChatResponse.builder()
                .conversationId(request.getConversationId())
                .answer(answer)
                .knowledgeCount(5)
                .build());
    }

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式聊天接口")
    public Flux<String> streamMessage(
            @RequestParam String conversationId,
            @RequestParam String userId,
            @RequestParam String message) {
        return chatService.streamReply(conversationId, userId, message)
                .map(part -> "data: {\"content\":\"" + sanitize(part) + "\"}\n\n");
    }

    @GetMapping("/chat/history/{conversationId}")
    @Operation(summary = "查询会话历史")
    public ResponseEntity<List<ChatMessage>> getHistory(@PathVariable String conversationId) {
        return ResponseEntity.ok(chatService.getConversationHistory(conversationId));
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\"", "\\\"");
    }
}
