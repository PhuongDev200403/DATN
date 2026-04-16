package com.buixuantruong.shopapp.service.impl;

import com.buixuantruong.shopapp.dto.ChatRequest;
import com.buixuantruong.shopapp.dto.response.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.Media;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ChatServiceImpl {

    private final ChatClient chatClient;

    public ChatServiceImpl(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public ChatResponse chat(ChatRequest request) {
        SystemMessage systemMessage = new SystemMessage("""
                You are Customer Service.AI.
                You should respond to customer's request with a funny voice.
                """);

        UserMessage userMessage = new UserMessage(request.message());
        Prompt prompt = new Prompt(systemMessage, userMessage);

        String chatResponse = chatClient
                .prompt(prompt)
                .call()
                .content();
        return new ChatResponse(chatResponse);
    }

    public ChatResponse chatWithImage(MultipartFile file, String message) {
        Media media = Media.builder()
                .mimeType(MimeTypeUtils.parseMimeType(file.getContentType()))
                .data(file.getResource())
                .build();

        String chatResponse = chatClient.prompt()
                .system("""
                        You are an AI assistant for customer service.
                        The customer sends an image and a message.
                        Respond quickly and answer the customer's question based on both.
                        """)
                .user(promptUserSpec -> promptUserSpec.media(media).text(message))
                .call()
                .content();

        return new ChatResponse(chatResponse);
    }
}
