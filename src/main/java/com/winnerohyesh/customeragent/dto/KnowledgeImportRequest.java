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
public class KnowledgeImportRequest {

    @NotBlank(message = "title 不能为空")
    @Schema(description = "知识标题", example = "退货政策")
    private String title;

    @NotBlank(message = "category 不能为空")
    @Schema(description = "知识分类", example = "policy")
    private String category;

    @NotBlank(message = "content 不能为空")
    @Schema(description = "知识内容", example = "签收后7天内可退货")
    private String content;

    @Schema(description = "来源链接", example = "https://example.com/help")
    private String sourceUrl;
}
