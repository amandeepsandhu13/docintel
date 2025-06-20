//package com.docintel.backend.controller;
//
//import com.docintel.backend.service.OpenAIEmbeddingService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/embedding")
//@RequiredArgsConstructor
//public class EmbeddingController {
//
//    private final OpenAIEmbeddingService embeddingService;
//
//    // Accepts raw text and returns embedding vector as JSON
//    @PostMapping
//    public ResponseEntity<double[]> getEmbedding(@RequestBody String text) {
//        try {
//            double[] embedding = embeddingService.getEmbedding(text);
//            return ResponseEntity.ok(embedding);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body(null);
//        }
//    }
//}
