package com.docintel.backend.controller;

import com.docintel.backend.dto.Chunk;
import com.docintel.backend.dto.ChunkResponse;
import com.docintel.backend.dto.SimpleAnalysisResult;
import com.docintel.backend.service.AdaptiveChunkingService;
import com.docintel.backend.service.DocumentAnalysisService;
import com.docintel.backend.service.EnhancedAdaptiveChunkingService;
import com.docintel.backend.util.DocumentParserUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.docintel.backend.util.FormRecognizerUtils.isAllowedExtension;


@RestController
@RequestMapping("/api/documents")
@Tag(name = "Document Analysis API", description = "Endpoints to analyze documents using Azure Form Recognizer & OpenAI embeddings")
public class DocumentAnalysisController {

    private final DocumentAnalysisService documentAnalysisService;

    @Autowired
    public DocumentAnalysisController(DocumentAnalysisService documentAnalysisService) {
        this.documentAnalysisService = documentAnalysisService;
    }

    @Operation(summary = "Upload a document and analyze")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDocument(
            @Parameter(description = "File to upload", required = true)
            @RequestPart("file") MultipartFile file,

            @Parameter(description = "Model type: 'invoice' or 'document'", example = "document", required = true)
            @RequestParam("modelType") String modelType) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: Uploaded file is empty.");
            }
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("File too large. Max 10MB allowed.");
            }

            SimpleAnalysisResult parsed = documentAnalysisService.analyzeAndGetResult(file.getBytes(), modelType);

            String docId = UUID.randomUUID().toString();
            documentAnalysisService.cacheParsedResult(docId, parsed);

            return ResponseEntity.ok(Map.of("docId", docId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Get full analysis result by document ID")
    @GetMapping("/{docId}/result")
    public ResponseEntity<?> getAnalysisResult(@PathVariable String docId) {
        SimpleAnalysisResult result = documentAnalysisService.getParsedResult(docId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
}