package com.docintel.backend.util;

import com.docintel.backend.dto.*;
import com.docintel.backend.service.UnstructuredEntityExtractionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DocumentParserUtil {

    private final UnstructuredEntityExtractionService entityExtractionService;

    @Autowired
    public DocumentParserUtil(UnstructuredEntityExtractionService entityExtractionService) {
        this.entityExtractionService = entityExtractionService;
    }

    public SimpleAnalysisResult parse(String json) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(json);
        JsonNode analyzeResult = root.path("analyzeResult");

        if (analyzeResult.isMissingNode() || analyzeResult.isEmpty()) {
            throw new IllegalArgumentException("Missing 'analyzeResult' in response.");
        }

        String content = analyzeResult.path("content").asText("");

        Set<String> extractedValues = new HashSet<>();

        // Parse key-value pairs
        List<KeyValue> keyValues = new ArrayList<>();
        Set<String> kvSeen = new HashSet<>();
        JsonNode kvPairsNode = analyzeResult.path("keyValuePairs");

        if (kvPairsNode.isArray()) {
            for (JsonNode kvNode : kvPairsNode) {
                String key = cleanText(FormRecognizerUtils.extractContent(kvNode.path("key")), true);
                String value = cleanText(FormRecognizerUtils.extractContent(kvNode.path("value")), false);

                if (key != null && value != null && !key.isEmpty() && !value.isEmpty()) {
                    String combined = key + "::" + value;
                    if (!kvSeen.contains(combined)) {
                        KeyValue kv = new KeyValue();
                        kv.setKey(key);
                        kv.setValue(value);
                        keyValues.add(kv);
                        kvSeen.add(combined);
                        extractedValues.add(key);
                        extractedValues.add(value);
                    }
                }
            }
        }

        // Parse tables
        List<Table> tables = new ArrayList<>();
        JsonNode tablesNode = analyzeResult.path("tables");

        if (tablesNode.isArray()) {
            for (JsonNode tableNode : tablesNode) {
                int rowCount = tableNode.path("rowCount").asInt(0);
                int colCount = tableNode.path("columnCount").asInt(0);
                List<List<String>> rows = new ArrayList<>();

                for (int i = 0; i < rowCount; i++) {
                    rows.add(new ArrayList<>(Collections.nCopies(colCount, "")));
                }

                JsonNode cellsNode = tableNode.path("cells");
                for (JsonNode cell : cellsNode) {
                    int rowIndex = cell.path("rowIndex").asInt();
                    int colIndex = cell.path("columnIndex").asInt();
                    String text = cell.path("content").asText("");

                    if (rowIndex < rowCount && colIndex < colCount) {
                        rows.get(rowIndex).set(colIndex, text);
                    }

                    extractedValues.add(text);
                }

                Table table = new Table();
                table.setRows(rows);
                tables.add(table);
            }
        }

        // Build unstructured content
        String unstructuredContent = content;
        for (String val : extractedValues) {
            unstructuredContent = unstructuredContent.replace(val, "");
        }
        unstructuredContent = unstructuredContent.replaceAll("\\s+", " ").trim();

        // Final result DTO
        SimpleAnalysisResult result = new SimpleAnalysisResult();
        result.setContent(content);
        result.setKeyValuePairs(keyValues);
        result.setTables(tables);
        result.setUnstructuredContent(unstructuredContent);

        // ✅ Call your enrichment module
        ExtractedEntities entities = entityExtractionService.extractEntities(unstructuredContent);
        result.setExtractedEntities(entities);

        return result;
    }

    private String cleanText(String raw, boolean isKey) {
        if (raw == null) return null;
        return isKey ? raw.trim().replaceAll("[:\\-\\s]+$", "") :
                raw.trim().replaceAll("^[:\\-\\s]+", "");
    }
}
