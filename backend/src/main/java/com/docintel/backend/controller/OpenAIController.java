package com.docintel.backend.controller;

import com.docintel.backend.service.OpenAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/ai")
public class OpenAIController {

    private final OpenAIService openAIService;

    @PostMapping("/ask")
    public ResponseEntity<String> ask(@RequestParam String chunk, @RequestParam String question) {
        try {
            String answer = openAIService.askQuestion(chunk, question);
            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

}
