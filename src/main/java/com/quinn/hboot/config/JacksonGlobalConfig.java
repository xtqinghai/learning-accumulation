package com.quinn.hboot.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import java.math.BigInteger;
import java.util.List;

@Configuration
public class JacksonGlobalConfig implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                ObjectMapper objectMapper = ((MappingJackson2HttpMessageConverter) converter).getObjectMapper();
                // ✅ 保留你原来的配置：忽略JSON反序列化时的未知字段，防止报错
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                // ✅ ========== 新增核心代码开始 ==========
                // 创建序列化规则模块
                SimpleModule simpleModule = new SimpleModule();
                // 1. 处理 基本类型 long + 包装类型 Long 转 String
                simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
                simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
                // 2. 【锦上添花】处理 BigInteger 类型（如果项目有用到），同样转String避免精度丢失
                simpleModule.addSerializer(BigInteger.class, ToStringSerializer.instance);
                // 将序列化规则注册到ObjectMapper
                objectMapper.registerModule(simpleModule);
                // ✅ ========== 新增核心代码结束 ==========
            }
        }
    }
}

