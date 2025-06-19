package com.docintel.backend.service;

import com.docintel.backend.dto.OpenAIMessage;
import com.docintel.backend.dto.OpenAIRequest;
import com.docintel.backend.dto.OpenAIResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.endpoint}")
    private String apiEndpoint;

    @Value("${openai.deployment}")
    private String deployment;

    @Value("${openai.api.version}")
    private String apiVersion;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String askQuestion(String context, String question) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        List<OpenAIMessage> messages = List.of(
                OpenAIMessage.builder().role("system").content("You are a legal document assistant.").build(),
                OpenAIMessage.builder().role("user").content("Context:\n" + context + "\n\nQuestion: " + question).build()
        );

        OpenAIRequest request = OpenAIRequest.builder().messages(messages).build();

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(apiEndpoint + "/openai/deployments/" + deployment + "/chat/completions?api-version=" + apiVersion))
                .header("Content-Type", "application/json")
                .header("api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                .build();

        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to call OpenAI: " + response.body());
        }

        OpenAIResponse aiResponse = objectMapper.readValue(response.body(), OpenAIResponse.class);
        return aiResponse.getChoices().get(0).getMessage().getContent();
    }

}
