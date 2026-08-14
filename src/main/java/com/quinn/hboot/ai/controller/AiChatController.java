package com.quinn.hboot.ai.controller;

import com.quinn.hboot.ai.model.AiChatRequest;
import com.quinn.hboot.ai.model.AiChatResponse;
import com.quinn.hboot.ai.service.AiChatService;
import com.quinn.hboot.common.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    public R<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        return R.ok(aiChatService.chat(request));
    }
}
