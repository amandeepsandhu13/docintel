package com.docintel.backend.controller;

import com.docintel.backend.dto.SimpleAnalysisResult;
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

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Document Analysis API", description = "Endpoints to analyze documents using Azure Form Recognizer")
public class DocumentAnalysisController {

    @Autowired
    private DocumentAnalysisService documentAnalysisService;

    @Operation(summary = "Upload a document and choose model (invoice or document)")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDocument(@Parameter(description = "File to upload", required = true)
                                                @RequestPart("file") MultipartFile file,
                                            @Parameter(description = "Model type: 'invoice' or 'document'", example = "invoice", required = true)
                                                @RequestParam("modelType") String modelType) {
        try {
            String operationLocation = documentAnalysisService.submitDocument(file.getBytes(), modelType);
            return ResponseEntity.ok(operationLocation);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to upload document: " + e.getMessage());
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
}
