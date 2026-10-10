package com.winnerohyesh.customeragent.controller;

import com.winnerohyesh.customeragent.dto.KnowledgeImportRequest;
import com.winnerohyesh.customeragent.entity.KnowledgeDocument;
import com.winnerohyesh.customeragent.service.KnowledgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "knowledge", description = "知识库管理接口")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @PostMapping("/knowledge/import")
    @Operation(summary = "导入知识库文档")
    public ResponseEntity<KnowledgeDocument> importKnowledge(@Valid @RequestBody KnowledgeImportRequest request) {
        return ResponseEntity.ok(knowledgeService.importDocument(request));
    }

    @GetMapping("/knowledge")
    @Operation(summary = "查询知识库列表")
    public ResponseEntity<List<KnowledgeDocument>> listKnowledge() {
        return ResponseEntity.ok(knowledgeService.listDocuments());
    }
}
