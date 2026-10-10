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
public class ConversationCreateRequest {

    @NotBlank(message = "userId 不能为空")
    @Schema(description = "用户ID", example = "user-001")
    private String userId;

    @Schema(description = "会话标题", example = "订单咨询")
    private String title;
}
