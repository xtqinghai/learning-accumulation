package com.quinn.hboot.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yu-qh
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("API 文档")
                        .version("1.0.0")
                        .description("系统 Knife4j 文档")
                        .contact(new Contact().name("开发者").email("438704315@qq.com")));
    }
}
