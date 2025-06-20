package com.docintel.backend.controller;

import com.docintel.backend.service.OpenAIService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final OpenAIService openAIService;

    public AIController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @PostMapping("/ask")
    public ResponseEntity<QnAResponse> askQuestion(@RequestBody QnARequest request) {
        try {
            String answer = openAIService.askQuestion(request.getContext(), request.getQuestion());
            return ResponseEntity.ok(new QnAResponse(answer));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new QnAResponse("Error: " + e.getMessage()));
        }
    }

    @Data
    public static class QnARequest {
        private String context;
        private String question;
    }

    @Data
    public static class QnAResponse {
        private final String answer;
    }
}
