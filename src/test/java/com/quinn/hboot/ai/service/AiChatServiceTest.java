package com.quinn.hboot.ai.service;

import com.quinn.hboot.ai.config.AiProperties;
import com.quinn.hboot.ai.model.AiChatMessage;
import com.quinn.hboot.ai.model.AiChatRequest;
import com.quinn.hboot.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class AiChatServiceTest {

    @Test
    void shouldRejectRequestWhenApiKeyIsNotConfigured() {
        ChatModel chatModel = mock(ChatModel.class);
        AiProperties properties = new AiProperties(
            true,
            "not-configured",
            "gpt-4o-mini",
            "system prompt"
        );
        AiChatService service = new AiChatService(chatModel, properties);

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> service.chat(requestWithLastRole("user"))
        );

        assertEquals("AI 密钥尚未配置，请设置 AI_API_KEY 后重试", exception.getMessage());
        verifyNoInteractions(chatModel);
    }

    @Test
    void shouldRequireTheLastMessageToComeFromUser() {
        ChatModel chatModel = mock(ChatModel.class);
        AiProperties properties = new AiProperties(true, "test-key", "test-model", "system prompt");
        AiChatService service = new AiChatService(chatModel, properties);

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> service.chat(requestWithLastRole("assistant"))
        );

        assertEquals("最后一条消息必须由用户发送", exception.getMessage());
        verifyNoInteractions(chatModel);
    }

    private AiChatRequest requestWithLastRole(String role) {
        return new AiChatRequest(List.of(new AiChatMessage(role, "你好")));
    }
}
