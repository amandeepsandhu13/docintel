package com.docintel.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Schema(description = "Semantic chunk of document with embedding")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Chunk {

    @Schema(description = "Chunk ID (optional)", example = "0")
    private int id;

    @Schema(description = "Index of the chunk", example = "0")
    private int index;

    @Schema(description = "Chunk text", example = "Section 1: This agreement is made...")
    private String text;

    @Schema(description = "Embedding vector representing the chunk", example = "[0.123, 0.456, 0.789]")
    private double[] embedding;

    @Schema(description = "Optional section title", example = "Section 1: Overview")
    private String sectionTitle;

    public Chunk(int index, String text) {
        this.index = index;
        this.text = text;
    }
}