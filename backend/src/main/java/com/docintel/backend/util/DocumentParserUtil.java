package com.docintel.backend.util;

import com.docintel.backend.dto.KeyValue;
import com.docintel.backend.dto.SimpleAnalysisResult;
import com.docintel.backend.dto.Table;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class DocumentParserUtil {
    public static SimpleAnalysisResult parse(String json) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode root = objectMapper.readTree(json);
    JsonNode analyzeResult = root.path("analyzeResult");

    String content = analyzeResult.path("content").asText();
    String fullContent = analyzeResult.path("content").asText("");

    // Parse key-value pairs
        List<KeyValue> keyValues = new ArrayList<>();
        Set<String> extractedValues = new HashSet<>();
        JsonNode kvPairsNode = analyzeResult.path("keyValuePairs");
        // 💡 Refined key-value parsing
        if (kvPairsNode.isArray()) {
            for (JsonNode kvNode : kvPairsNode) {
                String key = FormRecognizerUtils.extractContent(kvNode.path("key"));
                String value = FormRecognizerUtils.extractContent(kvNode.path("value"));

                if (key != null) key = key.trim().replaceAll("[:\\-\\s]+$", "");
                if (value != null) value = value.trim().replaceAll("^[:\\-\\s]+", "");

                if (key != null && value != null && !key.isEmpty() && !value.isEmpty()) {
                    KeyValue kv = new KeyValue();
                    kv.setKey(key);
                    kv.setValue(value);
                    keyValues.add(kv);
                    extractedValues.add(key);
                    extractedValues.add(value);
                }
            }
        }

        //  Parse tables
        List<Table> tables = new ArrayList<>();
        JsonNode tablesNode = analyzeResult.path("tables");

        if (tablesNode.isArray()) {
            for (JsonNode tableNode : tablesNode) {
                List<List<String>> rows = new ArrayList<>();
                int rowCount = tableNode.path("rowCount").asInt(0);
                int colCount = tableNode.path("columnCount").asInt(0);

                for (int i = 0; i < rowCount; i++) {
                    rows.add(new ArrayList<>(Collections.nCopies(colCount, "")));
                }

                JsonNode cellsNode = tableNode.path("cells");
                for (JsonNode cell : cellsNode) {
                    int rowIndex = cell.path("rowIndex").asInt();
                    int colIndex = cell.path("columnIndex").asInt();
                    String text = cell.path("content").asText("");
                    rows.get(rowIndex).set(colIndex, text);
                    extractedValues.add(text); // Also track table content
                }

                Table table = new Table();
                table.setRows(rows);
                tables.add(table);
            }
        }

        //  Unstructured content = full content - matched values
        String unstructuredContent = content;
        for (String val : extractedValues) {
            unstructuredContent = unstructuredContent.replace(val, "");
        }
        unstructuredContent = unstructuredContent.trim();

        //  Final DTO
        SimpleAnalysisResult result = new SimpleAnalysisResult();
        result.setContent(content);
        result.setKeyValuePairs(keyValues);
        result.setTables(tables);
        result.setUnstructuredContent(unstructuredContent);

        return result;
    }
}
