package com.winnerohyesh.customeragent.service;

import com.winnerohyesh.customeragent.dto.KnowledgeImportRequest;
import com.winnerohyesh.customeragent.entity.KnowledgeChunk;
import com.winnerohyesh.customeragent.entity.KnowledgeDocument;
import com.winnerohyesh.customeragent.repository.KnowledgeChunkRepository;
import com.winnerohyesh.customeragent.repository.KnowledgeDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeDocumentRepository knowledgeDocumentRepository;
    private final KnowledgeChunkRepository knowledgeChunkRepository;

    @Transactional
    public KnowledgeDocument importDocument(KnowledgeImportRequest request) {
        Instant now = Instant.now();

        KnowledgeDocument document = KnowledgeDocument.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .content(request.getContent())
                .sourceUrl(request.getSourceUrl())
                .createdAt(now)
                .updatedAt(now)
                .build();

        KnowledgeDocument savedDocument = knowledgeDocumentRepository.save(document);

        List<String> chunks = splitIntoChunks(request.getContent(), 800, 120);
        for (int i = 0; i < chunks.size(); i++) {
            KnowledgeChunk chunk = KnowledgeChunk.builder()
                    .documentId(savedDocument.getId())
                    .content(chunks.get(i))
                    .chunkIndex(i)
                    .createdAt(now)
                    .build();
            knowledgeChunkRepository.save(chunk);
        }

        return savedDocument;
    }

    public List<KnowledgeDocument> listDocuments() {
        return knowledgeDocumentRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<KnowledgeChunk> searchRelevantKnowledge(String question, int topK) {
        List<KnowledgeChunk> allChunks = knowledgeChunkRepository.findAll();
        if (allChunks.isEmpty()) {
            return List.of();
        }

        Set<String> questionTokens = tokenize(question);
        if (questionTokens.isEmpty()) {
            return List.of();
        }

        return allChunks.stream()
                .map(chunk -> new Candidate(chunk, score(questionTokens, chunk.getContent())))
                .filter(candidate -> candidate.score > 0.0)
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .limit(topK)
                .map(candidate -> candidate.chunk)
                .toList();
    }

    private List<String> splitIntoChunks(String content, int chunkSize, int overlap) {
        if (content == null || content.isBlank()) {
            return List.of();
        }

        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < content.length()) {
            int end = Math.min(start + chunkSize, content.length());
            String chunk = content.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }
            if (end >= content.length()) {
                break;
            }
            start = Math.max(start + chunkSize - overlap, end - overlap);
        }
        return chunks;
    }

    private double score(Set<String> questionTokens, String content) {
        Set<String> contentTokens = tokenize(content);
        if (contentTokens.isEmpty()) {
            return 0.0;
        }

        long overlap = questionTokens.stream().filter(contentTokens::contains).count();
        return overlap / (double) Math.max(1, questionTokens.size());
    }

    private Set<String> tokenize(String text) {
        return Arrays.stream(text.toLowerCase(Locale.ROOT)
                        .replaceAll("[^a-z0-9\\s]", " ")
                        .split("\\s+"))
                .filter(token -> !token.isBlank())
                .collect(Collectors.toSet());
    }

    private record Candidate(KnowledgeChunk chunk, double score) {
    }
}
