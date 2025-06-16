package com.docintel.backend.util;

import com.docintel.backend.dto.SimpleAnalysisResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DocumentParserUtil {


    public static SimpleAnalysisResult parse(String json) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(json);
        JsonNode documentsNode = root.path("analyzeResult").path("documents");
        System.out.println("Raw Azure response 1: " + json);

        if (!documentsNode.isArray() || documentsNode.size() == 0) {
            throw new Exception("No documents found in analyzeResult");
        }
        System.out.println("Raw Azure response: " + json);

        JsonNode fieldsNode = documentsNode.get(0).path("fields");

        String customerName = getStringValue(fieldsNode, "CustomerName");
        String invoiceNumber = getStringValue(fieldsNode, "InvoiceId");
        Double totalAmount = getDoubleValue(fieldsNode, "TotalAmount");
        String invoiceDate = getStringValue(fieldsNode, "InvoiceDate");
        String dueDate = getStringValue(fieldsNode, "DueDate");

        return new SimpleAnalysisResult(customerName, invoiceNumber, totalAmount, invoiceDate, dueDate);
    }

    private static String getStringValue(JsonNode node, String field) {
        JsonNode valueNode = node.path(field).path("valueString");
        return valueNode.isMissingNode() ? null : valueNode.asText();
    }

    private static Double getDoubleValue(JsonNode node, String field) {
        JsonNode valueNode = node.path(field).path("valueNumber");
        return valueNode.isMissingNode() ? null : valueNode.asDouble();
    }
}
