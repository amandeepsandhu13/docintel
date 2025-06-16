package com.docintel.backend.service;

import com.docintel.backend.dto.SimpleAnalysisResult;
import com.docintel.backend.util.DocumentParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;

@Service
public class DocumentAnalysisService {

    private final RestTemplate restTemplate;

    @Value("${azure.formrecognizer.endpoint}")
    private String endpoint;

    @Value("${azure.formrecognizer.apikey}")
    private String apikey;

    public DocumentAnalysisService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Submit document bytes to Form Recognizer and get operation-location URL for polling
    public String submitDocumentForAnalysis(MultipartFile file) throws Exception {
        String url = endpoint + "/formrecognizer/documentModels/prebuilt-document:analyze?api-version=2023-07-31";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Ocp-Apim-Subscription-Key", apikey);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);

        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);

        if (response.getStatusCode() == HttpStatus.ACCEPTED) {
            String operationLocation = response.getHeaders().getFirst("operation-location");
            if (operationLocation == null) {
                throw new Exception("Missing operation-location header");
            }
            return operationLocation;
        } else {
            throw new Exception("Failed to submit document for analysis: " + response.getStatusCode());
        }
    }

    // Poll operationLocation URL until result is ready or failed, return parsed SimpleAnalysisResult
    public SimpleAnalysisResult pollForResult(String operationLocation) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Ocp-Apim-Subscription-Key", apikey);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        int maxRetries = 5;
        int retryCount = 0;
        int delayMs = 5000;

        while (retryCount < maxRetries) {
            ResponseEntity<String> response = restTemplate.exchange(operationLocation, HttpMethod.GET, entity, String.class);
            System.out.println("waiting... at step 1");
            if (response.getStatusCode() == HttpStatus.OK) {
                String body = response.getBody();

                if (body.contains("\"status\":\"succeeded\"")) {
                    System.out.println("waiting... at step 2");
                    // Parse JSON and return DTO
                    try {
                        return DocumentParserUtil.parse(body);
                    }catch (Exception e){
                        e.printStackTrace();
                        throw new RuntimeException("Parsing failed: " + e.getMessage());
                    }
                } else if (body.contains("\"status\":\"failed\"")) {
                    System.out.println("waiting... at step 3");
                    throw new Exception("Document analysis failed: " + body);
                } else {
                    System.out.println("waiting... at step 4");
                    // Still running, wait and retry
                    Thread.sleep(delayMs);
                    retryCount++;
                    delayMs *= 2; // exponential backoff
                }
            } else if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                System.out.println("waiting... at step 5");
                // Rate limit hit - wait longer
                Thread.sleep(60000);
                retryCount++;
            } else {
                throw new Exception("Unexpected response: " + response.getStatusCode());
            }
        }
        throw new Exception("Exceeded max retries waiting for analysis result");
    }
}
