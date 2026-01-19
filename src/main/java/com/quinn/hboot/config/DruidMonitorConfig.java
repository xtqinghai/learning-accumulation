package com.quinn.hboot.config;

/**
 * @program: h-boot
 * @ClassName DruidMonitorConfig
 * @description: 说明：TODO
 * @author: yuqh
 * @create: 2026-01-15 15:10
 * @Version 1.0
 **/
// config/DruidMonitorConfig.java

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.support.jakarta.StatViewServlet;
import com.alibaba.druid.support.jakarta.WebStatFilter;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DruidMonitorConfig {

    /**
     * 1. 注册 Druid 监控页面的 Servlet
     * 关键：urlMappings 必须是 "/druid/*"
     */
    @Bean
    public ServletRegistrationBean<Servlet> druidStatViewServlet() {
        ServletRegistrationBean<Servlet> registrationBean =
                new ServletRegistrationBean<>();

        // 设置 Servlet
        registrationBean.setServlet(new StatViewServlet());

        // 设置 URL 映射
        registrationBean.addUrlMappings("/druid/*");

        // 初始化参数
        Map<String, String> initParams = new HashMap<>();
        initParams.put("loginUsername", "admin");      // 登录用户名
        initParams.put("loginPassword", "admin");      // 登录密码
        initParams.put("resetEnable", "false");        // 禁用重置按钮
        initParams.put("allow", "");                   // 允许所有IP访问，生产环境建议设置
        initParams.put("deny", "");                    // 拒绝的IP

        registrationBean.setInitParameters(initParams);

        return registrationBean;
    }

    /**
     * 2. 注册 Druid Web 统计过滤器
     * 用于收集 Web 请求统计信息
     */
    @Bean
    public FilterRegistrationBean<Filter> druidWebStatFilter() {
        FilterRegistrationBean<Filter> registrationBean =
                new FilterRegistrationBean<>();

        // 设置 Filter
        registrationBean.setFilter(new WebStatFilter());

        // 设置 URL 模式
        registrationBean.addUrlPatterns("/*");

        // 初始化参数
        Map<String, String> initParams = new HashMap<>();
        // 排除静态资源和监控页面本身
        initParams.put("exclusions",
                "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
        // 开启 session 统计
        initParams.put("sessionStatEnable", "true");
        // session 最大统计数
        initParams.put("sessionStatMaxCount", "1000");
        // 监控单个url调用的sql列表
        initParams.put("profileEnable", "true");

        registrationBean.setInitParameters(initParams);

        return registrationBean;
    }

    /**
     * 3. 注册 Druid 统计过滤器（SQL监控）
     * 这个过滤器会被自动应用到所有 Druid 数据源
     */
    @Bean
    public StatFilter statFilter() {
        StatFilter statFilter = new StatFilter();
        statFilter.setLogSlowSql(true);           // 开启慢SQL记录
        statFilter.setMergeSql(true);             // 合并SQL
        statFilter.setSlowSqlMillis(2000);        // 慢SQL阈值（毫秒）
        return statFilter;
    }

    /**
     * 4. 注册 Druid 防火墙过滤器（SQL防火墙）
     */
    @Bean
    public WallFilter wallFilter() {
        WallFilter wallFilter = new WallFilter();
        WallConfig wallConfig = new WallConfig();
        wallConfig.setMultiStatementAllow(true);  // 允许多条SQL语句
        wallFilter.setConfig(wallConfig);
        return wallFilter;
    }
}
