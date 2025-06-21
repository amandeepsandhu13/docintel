package com.docintel.backend.service;

import com.docintel.backend.dto.Chunk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EnhancedAdaptiveChunkingService {

    private static final int MAX_CHUNK_SIZE = 1000;
    private static final int MIN_CHUNK_SIZE = 50;

    private final OpenAIEmbeddingService embeddingService;

    @Autowired
    public EnhancedAdaptiveChunkingService(OpenAIEmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    public List<Chunk> chunkDocument(String content) {
        List<Chunk> initialChunks = hybridSplit(content);
        System.out.println("Initial chunks before merge: " + initialChunks.size());
        return mergeBySection(initialChunks);
    }

    // Hybrid splitter - section aware
    private List<Chunk> hybridSplit(String content) {
        List<Chunk> chunks = new ArrayList<>();
        int index = 0;

        // Normalize all newlines to spaces
        content = content.replaceAll("[\\n\\r]+", " ").replaceAll("\\s+", " ").trim();

        // Split on section numbers (legal style)
        String[] parts = content.split("(?=(\\d+\\.\\s))");

        if (parts.length == 1) {
            parts = content.split("(?<=[.?!])\\s+");
        }

        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.length() >= 10 && trimmed.matches(".*[a-zA-Z].*")) {
                chunks.add(new Chunk(index++, trimmed));
            }
        }
        return chunks;
    }

    // Merge chunks but preserve major section boundaries
    private List<Chunk> mergeBySection(List<Chunk> inputChunks) {
        List<Chunk> mergedChunks = new ArrayList<>();
        int index = 0;
        StringBuilder buffer = new StringBuilder();

        for (Chunk c : inputChunks) {
            // If current chunk starts with section header, flush buffer first
            if (c.getText().matches("^\\d+\\.\\s.*")) {
                if (buffer.length() >= MIN_CHUNK_SIZE) {
                    mergedChunks.add(new Chunk(index++, buffer.toString().trim()));
                    buffer = new StringBuilder();
                }
            }

            if (buffer.length() + c.getText().length() <= MAX_CHUNK_SIZE) {
                buffer.append(c.getText()).append(" ");
            } else {
                mergedChunks.add(new Chunk(index++, buffer.toString().trim()));
                buffer = new StringBuilder(c.getText()).append(" ");
            }
        }

        if (buffer.length() >= MIN_CHUNK_SIZE) {
            mergedChunks.add(new Chunk(index++, buffer.toString().trim()));
        }

        // Generate embeddings after final merge
        for (Chunk chunk : mergedChunks) {
            chunk.setEmbedding(getEmbedding(chunk.getText()));
        }

        System.out.println("Final merged chunks count: " + mergedChunks.size());
        return mergedChunks;
    }

    private double[] getEmbedding(String text) {
        try {
            return embeddingService.getEmbedding(text);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get embedding", e);
        }
    }
}
