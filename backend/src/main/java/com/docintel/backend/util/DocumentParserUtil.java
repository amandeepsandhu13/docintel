package com.docintel.backend.util;

import com.docintel.backend.dto.KeyValue;
import com.docintel.backend.dto.SimpleAnalysisResult;
import com.docintel.backend.dto.Table;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.regex.Pattern;


import java.util.*;

public class DocumentParserUtil {
    public static SimpleAnalysisResult parse(String json) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(json);
        JsonNode analyzeResult = root.path("analyzeResult");

        // Null/Empty check for analyzeResult
        if (analyzeResult.isMissingNode() || analyzeResult.isEmpty()) {
            throw new IllegalArgumentException("Missing 'analyzeResult' in response.");
        }

        String content = analyzeResult.path("content").asText("");
        String fullContent = analyzeResult.path("content").asText("");


        Set<String> extractedValues = new HashSet<>();

        // Key-Value Pair Parsing (Refined with cleaner)
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

        // Parse tables safely
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

// Safer unstructured content removal using regex with word boundaries and case-insensitive matching
        String unstructuredContent = content;
        for (String val : extractedValues) {
            unstructuredContent = unstructuredContent.replace(val, "");
        }
        unstructuredContent = unstructuredContent.trim();


        unstructuredContent = unstructuredContent.replaceAll("\\s+", " ").trim();

        // Final DTO
        SimpleAnalysisResult result = new SimpleAnalysisResult();
        result.setContent(content);
        result.setKeyValuePairs(keyValues);
        result.setTables(tables);
        result.setUnstructuredContent(unstructuredContent);

        return result;
    }

    // Reusable cleaner for key/value text
    private static String cleanText(String raw, boolean isKey) {
        if (raw == null) return null;
        return isKey ? raw.trim().replaceAll("[:\\-\\s]+$", "") :
                raw.trim().replaceAll("^[:\\-\\s]+", "");
    }
}
