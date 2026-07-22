package com.example.promptlib.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Primary
public class SpringAiService implements AiService {

    private static final Logger log = LoggerFactory.getLogger(SpringAiService.class);

    private final ChatModel chatModel;
    private final MockAiService mockAiService;

    // Use ObjectProvider to dynamically resolve ChatModel beans, 
    // avoiding ambiguity errors if multiple starters (OpenAI, Ollama) are present.
    public SpringAiService(ObjectProvider<ChatModel> chatModelProvider, MockAiService mockAiService) {
        this.mockAiService = mockAiService;
        
        List<ChatModel> models = chatModelProvider.orderedStream().collect(Collectors.toList());
        log.info("Detected {} active Spring AI ChatModel bean(s)", models.size());
        
        ChatModel selected = null;
        if (!models.isEmpty()) {
            // Prioritize OpenAI if present, else fallback to whatever is configured
            selected = models.stream()
                    .filter(m -> m.getClass().getSimpleName().toLowerCase().contains("openai"))
                    .findFirst()
                    .orElse(models.get(0));
            log.info("Selected ChatModel implementation: {}", selected.getClass().getName());
        } else {
            log.warn("No active ChatModel beans detected. Spring AI integrations will fall back to Mock responses.");
        }
        this.chatModel = selected;
    }

    @Override
    public String generate(String category, String prompt) {
        if (chatModel == null) {
            log.warn("No active Spring AI ChatModel bean detected. Delegating execution to MockAiService.");
            return mockAiService.generate(category, prompt);
        }

        try {
            log.info("Sending prompt to Spring AI ChatModel. Category: {}, Prompt length: {}", category, prompt.length());
            String systemInstruction = getInstructionForCategory(category);
            String fullPrompt = systemInstruction + "\n\nUser Request:\n" + prompt;
            
            // Call the ChatModel (uses spring-ai standard interface)
            return chatModel.call(fullPrompt);
        } catch (Exception e) {
            log.error("Exception occurred while calling Spring AI ChatModel: {}. Falling back to MockAiService.", e.getMessage());
            return "⚠️ AI Generation Failed: " + e.getMessage() + "\n\n[Displaying Fallback Mock Output below]\n\n" + mockAiService.generate(category, prompt);
        }
    }

    private String getInstructionForCategory(String category) {
        switch (category.toLowerCase()) {
            case "code":
                return "You are an expert software developer. Please generate clean, robust, and commented code. Return the code directly.";
            case "documentation":
                return "You are a professional technical writer. Generate comprehensive markdown documentation, API specifications, or guides based on the request.";
            case "testing":
                return "You are a senior QA automation engineer. Write thorough unit or integration tests with mocking where appropriate, covering success and edge cases.";
            case "design":
                return "You are a principal software architect. Create detailed system architecture descriptions, database schemas, and data flow designs.";
            default:
                return "You are a helpful AI assistant.";
        }
    }
}
