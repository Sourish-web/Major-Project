package com.jwt.implementation.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.jwt.implementation.dto.ChatGPTRequest;
import com.jwt.implementation.dto.ChatGPTResponse;
import com.jwt.implementation.dto.PromptRequest;

@Service
public class ChatGPTService {

    private final RestClient restClient;
   
    public ChatGPTService(RestClient restClient) {
        this.restClient = restClient;
    }

    @Value("${openapi.api.key}")
    private String apiKey;

    @Value("${openapi.api.model}")
    private String model;

    public String getChatResponse(PromptRequest promptRequest) {
        try {
            ChatGPTRequest chatGPTRequest = new ChatGPTRequest(
                    model,
                    List.of(new ChatGPTRequest.Message("user", promptRequest.prompt()))
            );

            System.out.println("Sending request to OpenAI: " + chatGPTRequest);

            ChatGPTResponse response = restClient.post()
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(chatGPTRequest)
                    .retrieve()
                    .onStatus(status -> status.value() >= 400, (req, res) -> {
                        String errorBody = res.getBody() != null ? new String(res.getBody().readAllBytes()) : "No body";
                        throw new RuntimeException("OpenAI API error: " + res.getStatusCode() + ", Body: " + errorBody);
                    })
                    .body(ChatGPTResponse.class);

            System.out.println("OpenAI Response: " + response);
            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                System.out.println("No choices in response");
                return "Empty choices from OpenAI API";
            }

            return response.choices().get(0).message().content();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}