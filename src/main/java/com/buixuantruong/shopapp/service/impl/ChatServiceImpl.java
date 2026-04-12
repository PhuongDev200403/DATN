package com.buixuantruong.shopapp.service.impl;

import com.buixuantruong.shopapp.dto.ChatRequest;
import com.buixuantruong.shopapp.dto.response.ChatResponse;
import com.buixuantruong.shopapp.dto.response.ListAndSearchVariantResponse;
import com.buixuantruong.shopapp.dto.response.VariantResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.Media;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;

@Service
public class ChatServiceImpl {

    private final ChatClient chatClient;

    public ChatServiceImpl(ChatClient.Builder builder){
        chatClient = builder.build();
    }

    public ChatResponse chat(ChatRequest request){

        SystemMessage systemMessage = new SystemMessage("""
                You are Customer Service.AI
                You should response customer's request with funny voice
                """);

        UserMessage userMessage = new UserMessage(request.message());

        Prompt prompt = new Prompt(systemMessage, userMessage);

        String chatResponse = chatClient
                            .prompt(prompt)
                            .call()
                            .content();
        return new ChatResponse(chatResponse);
    }

    public ChatResponse chatWithImage(MultipartFile file, String message){
        Media media = Media.builder()
                .mimeType(MimeTypeUtils.parseMimeType(file.getContentType()))
                .data(file.getResource())
                .build();

        String chatResponse = chatClient.prompt()
                                .system("""
                                        Bạn là AI đại diện cho dịch vụ chăm sóc khách hàng.
                                        Khách hàng gửi hình ảnh và bạn dựa vào hình ảnh và yêu cầu của khách hàng.
                                        Đưa ra phản hồi  nhanh chóng và giải đáp các thắc mắc của khác hàng.
                                        """)
                                .user(promptUserSpec -> promptUserSpec.media(media)
                                        .text(message)).call().content();

        return new ChatResponse(chatResponse);
    }

    public ChatResponse searchByImage(MultipartFile file){
        //List<ListAndSearchVariantResponse> thay ChatResponse là đụược
        Media media = Media.builder()
                .mimeType(MimeTypeUtils.parseMimeType(file.getContentType()))
                .data(file.getResource())
                .build();

        String response = chatClient.prompt()
                                .system("""
                                        Bạn là AI nhận diện sản phẩm.
                                        Khi khách hàng gửi yêu cầu tìm kiếm sản phẩm bằng hình ảnh.
                                        Bạn phải trả về thông tin của sản phẩm trong ảnh bảo gồm tên sản phẩm và đặc điểm để tìm kiếm.
                                        Ví dụ : iPhone 15 Pro Max, 256GB, màu xanh
                                        """)
                                .user(promptUserSpec -> promptUserSpec.media(media)
                                        .text("Mô tả sản phẩm trong ảnh")).call().content();
        return new ChatResponse(response);// đoạn này sẽ gọi hàm tìm kiếm bằng keyword
    }




}
