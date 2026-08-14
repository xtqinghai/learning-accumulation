package com.quinn.hboot.ai.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AiChatRequest(
    @NotEmpty(message = "聊天消息不能为空")
    @Size(max = 30, message = "单次最多携带 30 条聊天消息")
    List<@Valid AiChatMessage> messages
) {
}
