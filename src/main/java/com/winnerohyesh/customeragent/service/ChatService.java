package com.winnerohyesh.customeragent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.winnerohyesh.customeragent.config.DeepSeekProperties;
import com.winnerohyesh.customeragent.entity.ChatMessage;
import com.winnerohyesh.customeragent.entity.KnowledgeChunk;
import com.winnerohyesh.customeragent.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeepSeekService {

    private final WebClient deepSeekWebClient;
    private final DeepSeekProperties properties;
    private final ObjectMapper objectMapper;

    public String generateReply(List<ChatMessage> history, List<KnowledgeChunk> knowledgeChunks, String currentQuestion) {
        List<Map<String, Object>> messages = buildMessages(history, knowledgeChunks, currentQuestion);

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", properties.getModel());
        payload.put("messages", messages);
        payload.put("temperature", properties.getTemperature());
        payload.put("max_tokens", properties.getMaxTokens());
        payload.put("stream", false);

        String response = deepSeekWebClient.post()
                .uri("/chat/completions")
                .bodyValue(payload)
                .exchangeToMono(clientResponse -> {
                    if (clientResponse.statusCode().isError()) {
                        return clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new ApiException("DeepSeek API 调用失败: " + body)));
                    }
                    return clientResponse.bodyToMono(String.class);
                })
                .block();

        if (response == null || response.isBlank()) {
            throw new ApiException("DeepSeek 返回为空");
        }

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                return choices.get(0).path("message").path("content").asText();
            }
            return root.path("error").path("message").asText("无法解析 DeepSeek 返回结果");
        } catch (Exception ex) {
            throw new ApiException("解析 DeepSeek 响应失败", ex);
        }
    }

    public Flux<String> generateStreamReply(List<ChatMessage> history, List<KnowledgeChunk> knowledgeChunks, String currentQuestion) {
        List<Map<String, Object>> messages = buildMessages(history, knowledgeChunks, currentQuestion);

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", properties.getModel());
        payload.put("messages", messages);
        payload.put("temperature", properties.getTemperature());
        payload.put("max_tokens", properties.getMaxTokens());
        payload.put("stream", true);

        return deepSeekWebClient.post()
                .uri("/chat/completions")
                .header("Accept", "text/event-stream")
                .bodyValue(payload)
                .retrieve()
                .bodyToFlux(String.class)
                .flatMap(this::extractStreamText)
                .filter(text -> !text.isBlank());
    }

    private List<Map<String, Object>> buildMessages(List<ChatMessage> history, List<KnowledgeChunk> knowledgeChunks, String currentQuestion) {
        List<Map<String, Object>> messages = new ArrayList<>();

        String knowledgeContext = knowledgeChunks.isEmpty()
                ? "未匹配到相关知识库内容。"
                : knowledgeChunks.stream()
                        .map(KnowledgeChunk::getContent)
                        .reduce((a, b) -> a + "\n\n" + b)
                        .orElse("");

        messages.add(Map.of(
                "role", "system",
                "content", "你是一个专业客服助手。请使用知识库中的事实回答，无法确认时诚实说明，保持礼貌和简洁。"
        ));

        messages.add(Map.of(
                "role", "system",
                "content", "参考知识库:\n" + knowledgeContext
        ));

        for (ChatMessage message : history) {
            messages.add(Map.of(
                    "role", message.getRole(),
                    "content", message.getContent()
            ));
        }

        messages.add(Map.of(
                "role", "user",
                "content", currentQuestion
        ));

        return messages;
    }

    private Mono<String> extractStreamText(String rawChunk) {
        if (rawChunk == null || rawChunk.isBlank()) {
            return Mono.empty();
        }

        String line = rawChunk.trim();
        if (line.startsWith("data:")) {
            String data = line.substring(5).trim();
            if ("[DONE]".equals(data)) {
                return Mono.empty();
            }
            try {
                JsonNode node = objectMapper.readTree(data);
                JsonNode choices = node.path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    JsonNode delta = choices.get(0).path("delta");
                    if (delta.has("content")) {
                        return Mono.just(delta.path("content").asText());
                    }
                    return Mono.empty();
                }
            } catch (Exception ignored) {
                return Mono.empty();
            }
        }
        return Mono.empty();
    }
}
