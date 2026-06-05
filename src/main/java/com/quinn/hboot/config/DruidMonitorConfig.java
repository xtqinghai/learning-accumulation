package com.quinn.hboot.config;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.support.jakarta.StatViewServlet;
import com.alibaba.druid.support.jakarta.WebStatFilter;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import com.quinn.hboot.config.properties.DruidMonitorProperties;
import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.druid.monitor.enabled", havingValue = "true")
public class DruidMonitorConfig {

    private final DruidMonitorProperties monitorProperties;

    /**
     * 1. 注册 Druid 监控页面的 Servlet
     * 关键：urlMappings 必须是 "/druid/*"
     */
    @Bean
    public ServletRegistrationBean<Servlet> druidStatViewServlet() {
        Assert.hasText(monitorProperties.getUsername(), "app.druid.monitor.username must not be blank when monitor is enabled");
        Assert.hasText(monitorProperties.getPassword(), "app.druid.monitor.password must not be blank when monitor is enabled");

        ServletRegistrationBean<Servlet> registrationBean = new ServletRegistrationBean<>();
        registrationBean.setServlet(new StatViewServlet());
        registrationBean.addUrlMappings("/druid/*");

        Map<String, String> initParams = new HashMap<>();
        initParams.put("loginUsername", monitorProperties.getUsername());
        initParams.put("loginPassword", monitorProperties.getPassword());
        initParams.put("resetEnable", Boolean.toString(monitorProperties.isResetEnable()));
        if (StringUtils.hasText(monitorProperties.getAllow())) {
            initParams.put("allow", monitorProperties.getAllow());
        }
        if (StringUtils.hasText(monitorProperties.getDeny())) {
            initParams.put("deny", monitorProperties.getDeny());
        }
        registrationBean.setInitParameters(initParams);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<Filter> druidWebStatFilter() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new WebStatFilter());
        registrationBean.addUrlPatterns("/*");

        Map<String, String> initParams = new HashMap<>();
        initParams.put("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
        initParams.put("sessionStatEnable", Boolean.toString(monitorProperties.isSessionStatEnable()));
        initParams.put("sessionStatMaxCount", Integer.toString(monitorProperties.getSessionStatMaxCount()));
        initParams.put("profileEnable", Boolean.toString(monitorProperties.isProfileEnable()));
        registrationBean.setInitParameters(initParams);
        return registrationBean;
    }

    @Bean
    public StatFilter statFilter() {
        StatFilter statFilter = new StatFilter();
        statFilter.setLogSlowSql(monitorProperties.isLogSlowSql());
        statFilter.setMergeSql(monitorProperties.isMergeSql());
        statFilter.setSlowSqlMillis(monitorProperties.getSlowSqlMillis());
        return statFilter;
    }

    @Bean
    public WallFilter wallFilter() {
        WallFilter wallFilter = new WallFilter();
        WallConfig wallConfig = new WallConfig();
        wallConfig.setMultiStatementAllow(monitorProperties.isMultiStatementAllow());
        wallFilter.setConfig(wallConfig);
        return wallFilter;
    }
}
