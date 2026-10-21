package com.oraclejava.spring_ai_llama2.service;

import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LlamaAiService {

    @Autowired
    private OllamaChatModel chatModel;

    public String generateResult(String prompt) {
        ChatResponse response = chatModel.call(
                new Prompt(
                        prompt,
                        OllamaChatOptions.builder()
                                .model(OllamaModel.LLAMA2)
                                .temperature(0.4)
                                .build()
                ));
        return response.getResult().getOutput().getText();
    }
}
