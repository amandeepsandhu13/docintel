package com.docintel.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "DTO representing full document analysis result")
@Data
public class SimpleAnalysisResult {

    @Schema(description = "Full extracted text from document", example = "Invoice #12345 from ABC Corp")
    private String content;

    @Schema(description = "Extracted key-value pairs from the document")
    private List<KeyValue> keyValuePairs;

    @Schema(description = "Extracted tables from the document")
    private List<Table> tables;

    @Schema(description = "Chunks of the document after semantic chunking and embedding")
    private List<Chunk> chunks;

    @Schema(description = "Remaining unstructured content after structured extraction")
    private String unstructuredContent;

    private ExtractedEntities extractedEntities;

}