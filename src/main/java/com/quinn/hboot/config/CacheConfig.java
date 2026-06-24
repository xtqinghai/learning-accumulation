package com.quinn.hboot.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.quinn.hboot.config.properties.AppCacheProperties;
import com.quinn.hboot.system.cache.SystemCacheNames;
import com.quinn.hboot.workflow.common.constant.FlowConstant;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(AppCacheProperties properties) {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(
            buildCache(SystemCacheNames.MENU_VISIBLE_LIST, properties.getMenu(), properties.getMenuMaxSize()),
            buildCache(SystemCacheNames.MENU_VISIBLE_TREE, properties.getMenuTree(), properties.getMenuTreeMaxSize()),
            buildCache(SystemCacheNames.USER_MENU_LIST, properties.getUserAuth(), properties.getUserAuthMaxSize()),
            buildCache(SystemCacheNames.USER_MENU_TREE, properties.getUserAuth(), properties.getUserAuthMaxSize()),
            buildCache(SystemCacheNames.USER_PERMISSION_LIST, properties.getUserAuth(), properties.getUserAuthMaxSize()),
            buildCache(SystemCacheNames.ROLE_MENU_IDS, properties.getRoleMenu(), properties.getRoleMenuMaxSize()),
            buildCache(SystemCacheNames.DICT_LIST, properties.getDict(), properties.getDictMaxSize()),
            buildCache(SystemCacheNames.DICT_ITEMS, properties.getDict(), properties.getDictMaxSize()),
            buildCache(SystemCacheNames.DEPT_LIST, properties.getDept(), properties.getDeptMaxSize()),
            buildCache(SystemCacheNames.DEPT_TREE, properties.getDept(), properties.getDeptMaxSize()),
            buildCache(SystemCacheNames.USER_BY_ID, properties.getUserBase(), properties.getUserBaseMaxSize()),
            buildCache(SystemCacheNames.USER_BY_USERNAME, properties.getUserBase(), properties.getUserBaseMaxSize()),
            buildCache(SystemCacheNames.USER_ROLE_IDS, properties.getUserBase(), properties.getUserBaseMaxSize()),
            buildCache(SystemCacheNames.USER_ROLE_CODES, properties.getUserBase(), properties.getUserBaseMaxSize()),
            buildCache(SystemCacheNames.USER_ORG_POSTS, properties.getUserBase(), properties.getUserBaseMaxSize()),
            buildCache(FlowConstant.FLOW_CATEGORY_NAME, properties.getFlowCategory(), properties.getFlowCategoryMaxSize())
        ));
        return cacheManager;
    }

    private CaffeineCache buildCache(String cacheName, Duration ttl, long maxSize) {
        return new CaffeineCache(
            cacheName,
            Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .maximumSize(Math.max(maxSize, 1))
                .recordStats()
                .build(),
            true
        );
    }
}
