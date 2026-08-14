package com.quinn.hboot.ai.service;

import com.quinn.hboot.ai.config.AiProperties;
import com.quinn.hboot.ai.model.AiChatMessage;
import com.quinn.hboot.ai.model.AiChatRequest;
import com.quinn.hboot.ai.model.AiChatResponse;
import com.quinn.hboot.common.exception.BusinessException;
import com.quinn.hboot.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

    private final ChatModel chatModel;
    private final AiProperties properties;

    public AiChatResponse chat(AiChatRequest request) {
        validateConfiguration();
        validateConversation(request.messages());

        try {
            ChatResponse response = chatModel.call(new Prompt(toSpringAiMessages(request.messages())));
            String answer = response.getResult().getOutput().getText();
            if (answer == null || answer.isBlank()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 服务未返回有效内容，请稍后重试");
            }
            return new AiChatResponse(answer, properties.model());
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("AI chat request failed, model={}", properties.model(), ex);
            throw new BusinessException(
                ErrorCode.SYSTEM_ERROR,
                "AI 服务调用失败，请检查模型地址、密钥和模型配置"
            );
        }
    }

    private List<Message> toSpringAiMessages(List<AiChatMessage> messages) {
        List<Message> promptMessages = new ArrayList<>(messages.size() + 1);
        promptMessages.add(new SystemMessage(properties.systemPrompt()));
        for (AiChatMessage message : messages) {
            if ("assistant".equals(message.role())) {
                promptMessages.add(new AssistantMessage(message.content()));
            } else {
                promptMessages.add(new UserMessage(message.content()));
            }
        }
        return promptMessages;
    }

    private void validateConfiguration() {
        if (!properties.enabled()) {
            throw new BusinessException("AI 功能当前未启用");
        }
        if (!properties.isConfigured()) {
            throw new BusinessException("AI 密钥尚未配置，请设置 AI_API_KEY 后重试");
        }
    }

    private void validateConversation(List<AiChatMessage> messages) {
        AiChatMessage lastMessage = messages.get(messages.size() - 1);
        if (!"user".equals(lastMessage.role())) {
            throw new BusinessException("最后一条消息必须由用户发送");
        }
    }
}
