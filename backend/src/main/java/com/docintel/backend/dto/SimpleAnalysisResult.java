package com.docintel.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Schema(description = "DTO representing a document analysis result")
@Data
public class SimpleAnalysisResult {
    @Schema(description = "Full extracted text from document", example = "Invoice #12345 from ABC Corp")
    private String content;

    @Schema(description = "Extracted key-value pairs from the document")
    private List<KeyValue> keyValuePairs;

    @Schema(description = "Extracted tables from the document")
    private List<Table> tables;

    @Schema(description = "Chunks of the document content", example = "[{\"index\": 0, \"text\": \"Section 1: Overview\"}]")
    private List<Chunk> chunks;

    private String unstructuredContent;
    public String getUnstructuredContent() {
        return unstructuredContent;
    }

    public void setUnstructuredContent(String unstructuredContent) {
        this.unstructuredContent = unstructuredContent;
    }


    // Getter and Setter for content
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // Getter and Setter for keyValuePairs
    public List<KeyValue> getKeyValuePairs() {
        return keyValuePairs;
    }

    public void setKeyValuePairs(List<KeyValue> keyValuePairs) {
        this.keyValuePairs = keyValuePairs;
    }

    // Getter and Setter for tables
    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    // Getter and Setter for chunks
    public List<Chunk> getChunks() {
        return chunks;
    }

    public void setChunks(List<Chunk> chunks) {
        this.chunks = chunks;
    }


}
