package com.jwt.implementation.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jwt.implementation.dto.PromptRequest;
import com.jwt.implementation.service.ChatGPTService;

@RestController
@RequestMapping("/api/chat")
public class ChatGPTController {
	
	private final ChatGPTService chatGPTService;
	
	public ChatGPTController(ChatGPTService chatGPTService) {
		this.chatGPTService = chatGPTService;
	}
	
	@PostMapping
	public String chat(@RequestBody PromptRequest promptRequest ) {
		return chatGPTService.getChatResponse(promptRequest);
	}
	
	

}
