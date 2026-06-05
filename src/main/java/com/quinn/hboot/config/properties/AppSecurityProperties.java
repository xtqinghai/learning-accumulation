package com.quinn.hboot.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {

    private List<String> authExcludes = new ArrayList<>();
}
