package com.docintel.backend.controller;

import com.docintel.backend.dto.AskRequest;
import com.docintel.backend.service.OpenAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/ai")
public class OpenAIController {

    private final OpenAIService openAIService;

    @PostMapping("/ask")
    public ResponseEntity<String> ask(@RequestBody AskRequest request) {
        try {
            String answer = openAIService.askQuestion(request.getChunkContent(), request.getQuestion());
            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }


}
