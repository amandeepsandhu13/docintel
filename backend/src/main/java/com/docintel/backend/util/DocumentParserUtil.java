package com.docintel.backend.util;

import com.docintel.backend.dto.KeyValue;
import com.docintel.backend.dto.SimpleAnalysisResult;
import com.docintel.backend.dto.Table;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DocumentParserUtil {public static SimpleAnalysisResult parse(String json) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode root = objectMapper.readTree(json);
    JsonNode analyzeResult = root.path("analyzeResult");

    String content = analyzeResult.path("content").asText();

    // Parse key-value pairs
    List<KeyValue> keyValues = new ArrayList<>();
    JsonNode kvPairsNode = analyzeResult.path("keyValuePairs");
    if (kvPairsNode.isArray()) {
        for (JsonNode kvNode : kvPairsNode) {
            String key = kvNode.path("key").path("content").asText(null);
            String value = kvNode.path("value").path("content").asText(null);
            if (key != null && value != null) {
                KeyValue kv = new KeyValue();
                kv.setKey(key);
                kv.setValue(value);
                keyValues.add(kv);
            }
        }
    }

    // Parse tables
    List<Table> tables = new ArrayList<>();
    JsonNode tablesNode = analyzeResult.path("tables");
    if (tablesNode.isArray()) {
        for (JsonNode tableNode : tablesNode) {
            List<List<String>> rows = new ArrayList<>();
            int rowCount = tableNode.path("rowCount").asInt(0);
            int colCount = tableNode.path("columnCount").asInt(0);

            // Initialize empty 2D list of strings for table cells
            for (int i = 0; i < rowCount; i++) {
                rows.add(new ArrayList<>(Collections.nCopies(colCount, "")));
            }

            JsonNode cellsNode = tableNode.path("cells");
            for (JsonNode cell : cellsNode) {
                int rowIndex = cell.path("rowIndex").asInt();
                int colIndex = cell.path("columnIndex").asInt();
                String text = cell.path("content").asText("");
                rows.get(rowIndex).set(colIndex, text);
            }

            Table table = new Table();
            table.setRows(rows);
            tables.add(table);
        }
    }

    SimpleAnalysisResult result = new SimpleAnalysisResult();
    result.setContent(content);
    result.setKeyValuePairs(keyValues);
    result.setTables(tables);

    return result;
}
}
