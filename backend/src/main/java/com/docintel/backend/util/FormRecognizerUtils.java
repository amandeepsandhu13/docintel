package com.docintel.backend.util;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FormRecognizerUtils {
    private static final Logger log = LoggerFactory.getLogger(FormRecognizerUtils.class);

    public static String extractContent(JsonNode node) {

        if (node == null || node.isMissingNode()) return null;

        // Try direct content
        if (node.has("content")) {
            return node.get("content").asText(null);
        }

        // Fallback: elements array
        if (node.has("elements") && node.get("elements").isArray()) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode element : node.get("elements")) {
                if (element.has("content")) {
                    sb.append(element.get("content").asText()).append(" ");
                }
            }
            return sb.toString().trim();
        }

        return null;
    }
}
