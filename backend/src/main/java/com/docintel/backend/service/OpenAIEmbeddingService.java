package com.docintel.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class OpenAIEmbeddingService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.endpoint}")
    private String apiEndpoint;

    @Value("${openai.embedding.deployment}")
    private String deployment;

    @Value("${openai.api.version}")
    private String apiVersion;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public double[] getEmbedding(String inputText) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        // Prepare request body
        String requestBody = objectMapper.writeValueAsString(
                new Object() {
                    public final String input = inputText;
                }
        );

        // Send request
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(apiEndpoint + "/openai/deployments/" + deployment + "/embeddings?api-version=" + apiVersion))
                .header("Content-Type", "application/json")
                .header("api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch embeddings: " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode embeddingArray = root.get("data").get(0).get("embedding");

        double[] embedding = new double[embeddingArray.size()];
        for (int i = 0; i < embeddingArray.size(); i++) {
            embedding[i] = embeddingArray.get(i).asDouble();
        }

        return embedding;
    }
}
