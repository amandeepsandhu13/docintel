package com.docintel.backend.service;

import com.docintel.backend.dto.Chunk;
import com.docintel.backend.dto.SimpleAnalysisResult;
import com.docintel.backend.util.DocumentParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DocumentAnalysisService {

    private final RestTemplate restTemplate;
    private final Map<String, SimpleAnalysisResult> parsedDocs = new ConcurrentHashMap<>();


    @Value("${azure.formrecognizer.endpoint}")
    private String endpoint;

    @Value("${azure.formrecognizer.apikey}")
    private String apikey;

    @Autowired
    private AdaptiveChunkingService adaptiveChunkingService;

    public DocumentAnalysisService(RestTemplate restTemplate) {

        this.restTemplate = restTemplate;
    }

    // Upload file to Azure Form Recognizer prebuilt-document analyze endpoint
    public String submitDocument(byte[] fileBytes, String modelType) {

        if (!modelType.equalsIgnoreCase("invoice") && !modelType.equalsIgnoreCase("document")) {
            throw new IllegalArgumentException("Model type must be 'invoice' or 'document'");
        }

        String model = modelType.equalsIgnoreCase("invoice") ? "prebuilt-invoice" : "prebuilt-document";

        String url = endpoint + "/formrecognizer/documentModels/" + model + ":analyze?api-version=2023-07-31";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set("Ocp-Apim-Subscription-Key", apikey);

        HttpEntity<byte[]> entity = new HttpEntity<>(fileBytes, headers);

        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);

        if (response.getStatusCode() == HttpStatus.ACCEPTED) {
            // Operation-Location URL to poll later
            return response.getHeaders().getFirst("operation-location");
        } else {
            throw new RuntimeException("Failed to submit document for analysis: " + response.getStatusCode());
        }
    }

    // Poll operation-location URL until result is ready, return parsed result
    public SimpleAnalysisResult pollForResult(String operationLocation) {
        System.out.println("Polling operation location URL: " + operationLocation);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Ocp-Apim-Subscription-Key", apikey);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        int maxRetries = 5;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            ResponseEntity<String> response = restTemplate.exchange(operationLocation, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                String body = response.getBody();

                if (body.contains("\"status\":\"succeeded\"")) {
                    try {
                        SimpleAnalysisResult result = DocumentParserUtil.parse(body);

                        if (result.getContent() != null && !result.getContent().isEmpty()) {
                            result.setChunks(adaptiveChunkingService.chunkDocument(result.getContent()));
                        }

                        return result;
                    } catch (Exception e) {
                        throw new RuntimeException("Error parsing result: " + e.getMessage(), e);
                    }

                } else if (body.contains("\"status\":\"failed\"")) {
                    throw new RuntimeException("Azure Form Recognizer failed: " + body);
                }
            } else if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                // Azure is throttling
                sleepSilently(10000);
            } else {
                throw new RuntimeException("Unexpected response while polling: " + response.getStatusCode());
            }

            sleepSilently(5000);
            retryCount++;
        }

        throw new RuntimeException("Max retries exceeded while polling Form Recognizer.");
    }

    // Helper method to handle InterruptedException internally
    private void sleepSilently(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Polling thread interrupted", e);
        }
    }


    public SimpleAnalysisResult analyzeAndGetResult(byte[] fileBytes, String modelType) {
        // Call Azure Form Recognizer
        String operationLocation = submitDocument(fileBytes, modelType);
        return pollForResult(operationLocation); // returns full JSON as String
    }

    public void cacheParsedResult(String docId, SimpleAnalysisResult parsed) {
        parsedDocs.put(docId, parsed); // In-memory for now
    }
    public String getAnalyzedContent(String docId) {
        SimpleAnalysisResult result = parsedDocs.get(docId);
        return result != null ? result.getContent() : null;
    }

    public List<Chunk> getChunks(String docId) {
        SimpleAnalysisResult result = parsedDocs.get(docId);
        return result != null ? result.getChunks() : Collections.emptyList();
    }

    public SimpleAnalysisResult getParsedResult(String docId) {
        return parsedDocs.get(docId);

    }
}
