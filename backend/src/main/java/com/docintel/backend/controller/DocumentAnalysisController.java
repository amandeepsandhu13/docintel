package com.docintel.backend.controller;

import com.docintel.backend.dto.SimpleAnalysisResult;
import com.docintel.backend.service.DocumentAnalysisService;
import com.docintel.backend.util.DocumentParserUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Document Analysis API", description = "Endpoints to analyze documents using Azure Form Recognizer")
public class DocumentAnalysisController {

    @Autowired
    private DocumentAnalysisService documentAnalysisService;

    @PostMapping(value = "/analyze", consumes = "multipart/form-data")
    @Operation(summary = "Upload document and initiate analysis")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            String operationUrl = documentAnalysisService.submitDocumentForAnalysis(file);
            return ResponseEntity.ok(operationUrl);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error submitting document: " + e.getMessage());
        }
    }

    @GetMapping("/result")
    @Operation(summary = "Get analysis result using operation-location URL")
    public ResponseEntity<?> getResult(@RequestParam String operationLocation) {
        try {
            SimpleAnalysisResult result = documentAnalysisService.pollForResult(operationLocation);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error getting analysis result: " + e.getMessage());
        }
    }
}
