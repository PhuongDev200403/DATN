package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.dto.ChatRequest;
import com.buixuantruong.shopapp.dto.response.ChatResponse;
import com.buixuantruong.shopapp.service.impl.ChatServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController()
@RequestMapping("/api/v1/chatbot")
public class ChatController {

    @Autowired
    ChatServiceImpl chatService;

    @PostMapping(value = "/chat", produces = "application/json; charset=UTF-8")
    ChatResponse chat(@RequestBody ChatRequest request){
        return chatService.chat(request);
    }

    @PostMapping(value = "chat-with-image", produces = "application/json; charset=UTF-8")
    ChatResponse chatWithImage(@RequestParam("file")MultipartFile file,
                         @RequestParam("message") String message){
        return chatService.chatWithImage(file, message);
    }

    @PostMapping(value = "/search-by-image")
    ChatResponse searchByImage(@RequestParam("file") MultipartFile file){
        return chatService.searchByImage(file);
    }

}
