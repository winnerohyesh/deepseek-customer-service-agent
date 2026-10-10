package com.winnerohyesh.customeragent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    @NotBlank(message = "conversationId 不能为空")
    @Schema(description = "会话ID", example = "conv-123")
    private String conversationId;

    @NotBlank(message = "userId 不能为空")
    @Schema(description = "用户ID", example = "user-001")
    private String userId;

    @NotBlank(message = "message 不能为空")
    @Schema(description = "用户消息", example = "我的订单还没发货，怎么处理？")
    private String message;
}
