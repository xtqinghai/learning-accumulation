package com.quinn.hboot.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public record AiProperties(
    boolean enabled,
    String apiKey,
    String model,
    String systemPrompt
) {

    public boolean isConfigured() {
        return enabled && apiKey != null && !apiKey.isBlank() && !"not-configured".equals(apiKey);
    }
}
