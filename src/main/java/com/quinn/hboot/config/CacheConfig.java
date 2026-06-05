package com.quinn.hboot.config;

import com.quinn.hboot.system.cache.SystemCacheNames;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(
            SystemCacheNames.MENU_VISIBLE_LIST,
            SystemCacheNames.MENU_VISIBLE_TREE,
            SystemCacheNames.USER_MENU_LIST,
            SystemCacheNames.USER_PERMISSION_LIST,
            SystemCacheNames.ROLE_MENU_IDS,
            SystemCacheNames.DICT_LIST,
            SystemCacheNames.DICT_ITEMS,
            SystemCacheNames.DEPT_LIST,
            SystemCacheNames.DEPT_TREE,
            SystemCacheNames.USER_BY_ID,
            SystemCacheNames.USER_BY_USERNAME,
            SystemCacheNames.USER_ROLE_IDS,
            SystemCacheNames.USER_ROLE_CODES,
            SystemCacheNames.USER_ORG_POSTS
        );
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }
}
