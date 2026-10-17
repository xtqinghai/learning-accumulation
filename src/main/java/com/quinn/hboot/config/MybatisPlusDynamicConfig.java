package com.quinn.hboot.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: h-boot
 * @ClassName MybatisPlusDynamicConfig
 * @description: 说明：TODO
 * @author: yuqh
 * @create: 2026-01-15 14:35
 * @Version 1.0
 **/
// config/MybatisPlusDynamicConfig.java
@Configuration
@MapperScan(basePackages = "com.quinn.hboot.**.mapper")
public class MybatisPlusDynamicConfig {

    /**
     * MyBatis Plus 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        paginationInnerInterceptor.setOverflow(true);
        paginationInnerInterceptor.setMaxLimit(1000L);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        // 乐观锁插件（可选）
        // interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        return interceptor;
    }

    /**
     * 防止全表更新与删除插件
     */
    @Bean
    public BlockAttackInnerInterceptor blockAttackInnerInterceptor() {
        return new BlockAttackInnerInterceptor();
    }
}
