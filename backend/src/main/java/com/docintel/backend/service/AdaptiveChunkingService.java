package com.docintel.backend.service;

import com.docintel.backend.dto.Chunk;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdaptiveChunkingService {

    public List<Chunk> chunkDocument(String content) {
        List<Chunk> chunks = new ArrayList<>();
        String[] parts = content.split("\\n\\n|(?=Section\\s+\\d+)|(?=\\d+\\.)");

        int index = 0;
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                Chunk chunk = new Chunk();
                chunk.setIndex(index++);
                chunk.setText(trimmed);
                chunks.add(chunk);
            }
        }

        return chunks;
    }
}

