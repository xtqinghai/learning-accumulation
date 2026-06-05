package com.quinn.hboot.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j/SpringDoc 核心配置类
 * 解决 No OpenAPI resource found for group: swagger-config 错误
 */
@Configuration
@ConditionalOnProperty(name = "app.api-docs.enabled", havingValue = "true")
public class SwaggerConfig {

    /**
     * 定义默认分组（对应 swagger-config），指定要扫描的接口包路径
     * 重点：basePackages 要替换成你自己的接口所在包名！
     */
    @Bean
    public GroupedOpenApi swaggerConfigGroup() {
        // 这里的 group 名称必须是 swagger-config（和错误中的分组名一致）
        return GroupedOpenApi.builder()
                .group("swagger-config")
                // 1. 确认包路径是实际存在的（可多包逗号分隔，也可数组形式）
                .packagesToScan(
                        "com.quinn.hboot.system.controller",
                        "com.quinn.hboot.codegen.controller"
                )
                // 2. 可选：禁用过滤（确保所有接口都被扫描，排除过滤导致的不展示）
                .pathsToMatch("/**")
                .build();
    }

    /**
     * 定义 OpenAPI 文档的基础信息（标题、版本、描述等）
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("H-Boot API 文档")
                        .description("基于 Spring Boot 3 的后端 API 接口文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Quinn")
                                .email("quinn@example.com")
                                .url(""))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .components(new Components()
                        .addSecuritySchemes("bearer-token", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("Bearer Token")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-token"));
    }
}
