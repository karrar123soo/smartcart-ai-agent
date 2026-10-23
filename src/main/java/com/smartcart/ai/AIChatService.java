package com.smartcart.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AIChatService {

	private static final String SYSTEM_INSTRUCTIONS = """
			You are the AI Shopping Assistant for the SmartCart store.
			Your job is to assist customers with product inquiries, search, and budget-friendly recommendations.

			Rules:
			1. You MUST ALWAYS use the provided product tools to answer questions about products, availability, and prices in the store:
			   - Use `searchProducts` to search for products by name, category, or description keywords.
			   - Use `getProductsByMaxPrice` when customers specify a budget or maximum price limit.
			2. NEVER invent, hallucinate, assume, or guess products, prices, categories, or descriptions. You must ONLY reference products that are actually returned by the tools.
			3. When recommending or describing products to the user, always provide relevant product details including name, category, price, and description.
			4. If no matching products are found from the tools, politely inform the customer that no matching products are currently available in the SmartCart catalog.
			5. Maintain a helpful, polite, and professional tone.
			""";

	private final ChatClient chatClient;

	public AIChatService(ChatClient.Builder chatClientBuilder, ProductTools productTools) {
		this.chatClient = chatClientBuilder
				.defaultTools(productTools)
				.defaultSystem(SYSTEM_INSTRUCTIONS)
				.build();
	}

	public String chat(String message) {
		return chatClient.prompt()
				.user(message)
				.call()
				.content();
	}

}
