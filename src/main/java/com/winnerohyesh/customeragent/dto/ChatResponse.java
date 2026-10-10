package com.winnerohyesh.customeragent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    @Schema(description = "会话ID")
    private String conversationId;

    @Schema(description = "回答内容")
    private String answer;

    @Schema(description = "参考知识条数")
    private int knowledgeCount;
}
