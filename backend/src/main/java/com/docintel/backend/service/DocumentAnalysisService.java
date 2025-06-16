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


    // Upload file to Azure Form Recognizer prebuilt-document analyze endpoint
    public String submitDocument(byte[] fileBytes, String modelType) {
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
    public SimpleAnalysisResult pollForResult(String operationLocation) throws InterruptedException {
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
                    // Parse JSON into DTO and return
                    try{
                        return DocumentParserUtil.parse(body);
                    }catch (Exception e){
                        System.out.println(e.getMessage());
                    }
                } else if (body.contains("\"status\":\"failed\"")) {
                    throw new RuntimeException("Document analysis failed: " + body);
                } else {
                    // Still running, wait before retry
                    Thread.sleep(50000);
                    retryCount++;
                }
            } else if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                // Rate limited - wait longer
                Thread.sleep(600000);
                retryCount++;
            } else {
                throw new RuntimeException("Unexpected response status: " + response.getStatusCode());
            }
        }

        throw new RuntimeException("Max retries exceeded waiting for analysis result");
    }
}
