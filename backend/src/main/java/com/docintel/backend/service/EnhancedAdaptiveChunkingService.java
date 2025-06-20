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
        return mergeByLength(initialChunks);
    }

    // Bulletproof hybrid splitting logic
    private List<Chunk> hybridSplit(String content) {
        List<Chunk> chunks = new ArrayList<>();
        int index = 0;

        // Normalize all newlines and spaces
        content = content.replaceAll("[\\r\\n]+", " ").replaceAll("\\s+", " ").trim();

        // 1️⃣ Try section numbers first (for legal contracts)
        String[] parts = content.split("(?=\\d+\\.\\s)");

        // 2️⃣ If no section numbers found, check for ALL CAPS legal headers (OCR-friendly)
        if (parts.length == 1) {
            parts = content.split("(?=\\b[A-Z][A-Z\\s]{5,}\\b)");
        }

        // 3️⃣ If still nothing, fallback to sentence splitting
        if (parts.length == 1) {
            parts = content.split("(?<=[.?!])\\s+");
        }

        // Build initial chunks
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.length() >= 10 && trimmed.matches(".*[a-zA-Z].*")) {
                chunks.add(new Chunk(index++, trimmed));
            }
        }
        return chunks;
    }

    // Smart merge purely by length — keep chunks reasonably sized
    private List<Chunk> mergeByLength(List<Chunk> inputChunks) {
        List<Chunk> mergedChunks = new ArrayList<>();
        int index = 0;
        StringBuilder buffer = new StringBuilder();

        for (Chunk c : inputChunks) {
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
