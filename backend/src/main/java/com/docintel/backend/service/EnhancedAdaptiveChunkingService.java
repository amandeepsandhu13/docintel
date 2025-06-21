package com.docintel.backend.service;

import com.docintel.backend.dto.Chunk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EnhancedAdaptiveChunkingService {

    private static final int MAX_CHUNK_SIZE = 1000; // Max chars after merging
    private static final double SIMILARITY_THRESHOLD = 0.85;

    private final OpenAIEmbeddingService embeddingService;

    @Autowired
    public EnhancedAdaptiveChunkingService(OpenAIEmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    public List<Chunk> chunkDocument(String content) {
        List<Chunk> initialChunks = initialSplit(content);
        System.out.println("Initial chunks generated: " + initialChunks.size());
        return mergeSimilarChunks(initialChunks);
    }

    // Step 1: Improved initial split with auto-labeling
    private List<Chunk> initialSplit(String content) {
        List<Chunk> chunks = new ArrayList<>();
        int index = 0;

        // Split into sentences and sections
        String[] parts = content.split("(?=\\n?\\d+\\.\\s)|(?<=\\.)\\s+");

        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.length() >= 10 && trimmed.matches(".*[a-zA-Z].*")) {
                Chunk chunk = new Chunk(index++, trimmed);
                chunk.setSectionTitle(extractHeading(trimmed));  // Auto label assigned here
                chunks.add(chunk);
            }
        }

        return chunks;
    }

    // Extract possible heading/section title
    private String extractHeading(String text) {
        if (text.matches("^\\d+\\.\\s+[A-Za-z ]+.*")) {
            // E.g. "1. Scope of Services ..."
            String[] splitParts = text.split("\\.\\s+", 2);
            if (splitParts.length > 1) {
                String possibleTitle = splitParts[1].split("\\n")[0];
                if (possibleTitle.length() > 50) {
                    possibleTitle = possibleTitle.substring(0, 50) + "...";
                }
                return possibleTitle;
            }
        }
        // fallback generic title
        return "Section";
    }

    // Step 2: Merge highly similar short chunks based on semantic similarity
    private List<Chunk> mergeSimilarChunks(List<Chunk> initialChunks) {
        List<Chunk> mergedChunks = new ArrayList<>();
        Chunk current = null;

        for (Chunk c : initialChunks) {
            if (current == null) {
                current = c;
                current.setEmbedding(getEmbedding(current.getText()));
            } else {
                double[] currentEmbedding = current.getEmbedding();
                double[] nextEmbedding = getEmbedding(c.getText());
                double similarity = cosineSimilarity(currentEmbedding, nextEmbedding);
                int combinedLength = current.getText().length() + c.getText().length();

                if (similarity > SIMILARITY_THRESHOLD && combinedLength < MAX_CHUNK_SIZE) {
                    current.setText(current.getText() + " " + c.getText());
                    current.setEmbedding(getEmbedding(current.getText()));
                } else {
                    mergedChunks.add(current);
                    current = c;
                    current.setEmbedding(nextEmbedding);
                }
            }
        }

        if (current != null) {
            mergedChunks.add(current);
        }

        // Re-index after merging
        for (int i = 0; i < mergedChunks.size(); i++) {
            mergedChunks.get(i).setIndex(i);
        }

        System.out.println("Final merged chunks: " + mergedChunks.size());
        return mergedChunks;
    }

    private double[] getEmbedding(String text) {
        try {
            return embeddingService.getEmbedding(text);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get embedding for chunk", e);
        }
    }

    private double cosineSimilarity(double[] vec1, double[] vec2) {
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < vec1.length; i++) {
            dot += vec1[i] * vec2[i];
            normA += vec1[i] * vec1[i];
            normB += vec2[i] * vec2[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB) + 1e-10);
    }
}
