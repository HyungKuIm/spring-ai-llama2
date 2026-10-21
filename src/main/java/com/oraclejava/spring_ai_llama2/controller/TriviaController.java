package com.oraclejava.spring_ai_llama2.controller;

import com.oraclejava.spring_ai_llama2.service.TriviaEmbeddingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trivia")
public class TriviaController {

    private final TriviaEmbeddingService service;

    public TriviaController(TriviaEmbeddingService service) {
        this.service = service;
    }

    @PostMapping("/embedding")
    public Map<String, Object> embedding() {
        int count = service.generateEmbedings();

        return Map.of(
                "message", "Embedding completed",
                "processed", count
        );
    }

    @GetMapping
    public List<Map<String, Object>> searchTriviaEmbeddings(@RequestParam String query) {
        List<Map<String, Object>> embeddings = service.searchTriviaEmbeddings(query);
        return embeddings;
    }
}
