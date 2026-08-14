package com.quinn.hboot.ai.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AiChatMessage(
    @NotBlank(message = "消息角色不能为空")
    @Pattern(regexp = "user|assistant", message = "消息角色只支持 user 或 assistant")
    String role,

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 8000, message = "单条消息不能超过 8000 个字符")
    String content
) {
}
