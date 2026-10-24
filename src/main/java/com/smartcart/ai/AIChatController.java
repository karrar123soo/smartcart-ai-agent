package com.smartcart.ai;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AIChatController {

	private final AIChatService aiChatService;

	public AIChatController(AIChatService aiChatService) {
		this.aiChatService = aiChatService;
	}

	@PostMapping("/chat")
	public String chat(@RequestBody ChatRequest request) {
		return aiChatService.chat(request.message());
	}

	public record ChatRequest(String message) {

	}

}
