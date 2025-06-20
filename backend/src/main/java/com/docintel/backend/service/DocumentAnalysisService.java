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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DocumentAnalysisService {

    private final RestTemplate restTemplate;
    private final Map<String, SimpleAnalysisResult> parsedDocs = new ConcurrentHashMap<>();
    @Autowired
    private DocumentParserUtil documentParserUtil;


    @Value("${azure.formrecognizer.endpoint}")
    private String endpoint;

    @Value("${azure.formrecognizer.apikey}")
    private String apikey;

    private final EnhancedAdaptiveChunkingService chunkingService;

    @Autowired
    public DocumentAnalysisService(RestTemplate restTemplate, EnhancedAdaptiveChunkingService chunkingService) {
        this.restTemplate = restTemplate;
        this.chunkingService = chunkingService;
    }

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
            return response.getHeaders().getFirst("operation-location");
        } else {
            throw new RuntimeException("Failed to submit document for analysis: " + response.getStatusCode());
        }
    }

    public SimpleAnalysisResult pollForResult(String operationLocation) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Ocp-Apim-Subscription-Key", apikey);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        int retries = 0;
        while (retries < 5) {
            ResponseEntity<String> response = restTemplate.exchange(operationLocation, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                String body = response.getBody();
                if (body.contains("\"status\":\"succeeded\"")) {
                    try {
                        //SimpleAnalysisResult result = DocumentParserUtil.parse(body);
                        SimpleAnalysisResult result = documentParserUtil.parse(body);

                        if (result.getContent() != null && !result.getContent().isEmpty()) {
                            result.setChunks(chunkingService.chunkDocument(result.getContent()));
                        }
                        return result;
                    } catch (Exception e) {
                        throw new RuntimeException("Error parsing result: " + e.getMessage(), e);
                    }
                } else if (body.contains("\"status\":\"failed\"")) {
                    throw new RuntimeException("Form Recognizer failed: " + body);
                }
            }
            sleep(5000);
            retries++;
        }
        throw new RuntimeException("Max retries exceeded while polling Form Recognizer.");
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    public SimpleAnalysisResult analyzeAndGetResult(byte[] fileBytes, String modelType) {
        String operationLocation = submitDocument(fileBytes, modelType);
        return pollForResult(operationLocation);
    }

    public void cacheParsedResult(String docId, SimpleAnalysisResult parsed) {
        parsedDocs.put(docId, parsed);
    }

    public SimpleAnalysisResult getParsedResult(String docId) {
        return parsedDocs.get(docId);
    }
}
