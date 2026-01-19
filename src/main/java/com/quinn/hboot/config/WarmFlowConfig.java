package com.quinn.hboot.config;

import com.quinn.hboot.module.flow.handle.CustomDataFillHandler;
import com.quinn.hboot.module.flow.handle.CustomPermissionHandler;
import org.dromara.warm.flow.core.handler.DataFillHandler;
import org.dromara.warm.flow.core.handler.PermissionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * warm-flow配置文件
 *
 * @author warm
 */
@Configuration
public class WarmFlowConfig {

    /**
     * 自定义填充 （可配置文件注入，也可用@Bean/@Component方式）
     */
    @Bean
    public DataFillHandler dataFillHandler() {
        return new CustomDataFillHandler();
    }

    /**
     * 权限校验器 （可配置文件注入，也可用@Bean/@Component方式）
     */
    @Bean
    public PermissionHandler permissionHandler() {
        return new CustomPermissionHandler();
    }

//    /**
//     * 全局租户处理器（可配置文件注入，也可用@Bean/@Component方式）
//     */
//    @Bean
//    public TenantHandler tenantHandler() {
//        return new CustomTenantHandler();
//    }
}
