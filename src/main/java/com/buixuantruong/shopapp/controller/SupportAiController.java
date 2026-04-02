package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.service.SupportAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/support")
@RequiredArgsConstructor
public class SupportAiController {
    private final SupportAiService supportAiService;

    @PostMapping("/chat")
    public ApiResponse<String> chat(@RequestParam String query) {
        String response = supportAiService.chatWithSupport(query);
        return ApiResponse.<String>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(response)
                .build();
    }
}
