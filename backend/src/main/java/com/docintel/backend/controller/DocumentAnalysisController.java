package com.docintel.backend.controller;

import com.docintel.backend.dto.Chunk;
import com.docintel.backend.dto.SimpleAnalysisResult;
import com.docintel.backend.service.AdaptiveChunkingService;
import com.docintel.backend.service.DocumentAnalysisService;
import com.docintel.backend.util.DocumentParserUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Document Analysis API", description = "Endpoints to analyze documents using Azure Form Recognizer")
public class DocumentAnalysisController {

    @Autowired
    private DocumentAnalysisService documentAnalysisService;

    @Autowired
    private AdaptiveChunkingService adaptiveChunkingService;

    @Operation(summary = "Upload a document and choose model (invoice or document)")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDocument(@Parameter(description = "File to upload", required = true)
                                                @RequestPart("file") MultipartFile file,
                                             @Parameter(description = "Model type: 'invoice' or 'document'", example = "invoice", required = true)
                                                @RequestParam("modelType") String modelType) {
        try {
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("File too large. Max 10MB allowed.");
            }

            String operationLocation = documentAnalysisService.submitDocument(file.getBytes(), modelType);
            return ResponseEntity.ok(operationLocation);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error","Failed to upload document" ,"details", e.getMessage()));
        }
    }

    @Operation(summary = "Get analysis result from operation-location URL")
    @GetMapping("/result")
    public ResponseEntity<?> getAnalysisResult(@RequestParam String operationLocation) {
        try {
            SimpleAnalysisResult result = documentAnalysisService.pollForResult(operationLocation);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error getting analysis result: " + e.getMessage());
        }
    }
    @Operation(summary = "Fetch content from operation-location and return adaptive chunks")
    @GetMapping("/chunk")
    public ResponseEntity<List<Chunk>> chunkByOperationLocation(@RequestParam String operationLocation) {
        try {
            // Step 1: Get extracted document result
            SimpleAnalysisResult result = documentAnalysisService.pollForResult(operationLocation);

            // Step 2: Extract content and chunk it
            List<Chunk> chunks = adaptiveChunkingService.chunkDocument(result.getContent());

            return ResponseEntity.ok(chunks);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }
}
