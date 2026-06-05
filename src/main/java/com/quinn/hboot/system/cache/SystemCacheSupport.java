package com.quinn.hboot.system.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class SystemCacheSupport {

    private final CacheManager cacheManager;

    public SystemCacheSupport(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void clearMenuCaches() {
        clear(SystemCacheNames.MENU_VISIBLE_LIST);
        clear(SystemCacheNames.MENU_VISIBLE_TREE);
    }

    public void clearRoleMenuCaches() {
        clear(SystemCacheNames.ROLE_MENU_IDS);
    }

    public void evictRoleMenuCache(Long roleId) {
        evict(SystemCacheNames.ROLE_MENU_IDS, roleId);
    }

    public void clearUserAuthorizationCaches() {
        clear(SystemCacheNames.USER_MENU_LIST);
        clear(SystemCacheNames.USER_PERMISSION_LIST);
    }

    public void clearUserBaseCaches() {
        clear(SystemCacheNames.USER_BY_ID);
        clear(SystemCacheNames.USER_BY_USERNAME);
        clear(SystemCacheNames.USER_ROLE_IDS);
        clear(SystemCacheNames.USER_ROLE_CODES);
        clear(SystemCacheNames.USER_ORG_POSTS);
    }

    public void evictUserBase(Long userId, String username) {
        evict(SystemCacheNames.USER_BY_ID, userId);
        evict(SystemCacheNames.USER_ROLE_IDS, userId);
        evict(SystemCacheNames.USER_ROLE_CODES, userId);
        evict(SystemCacheNames.USER_ORG_POSTS, userId);
        evict(SystemCacheNames.USER_MENU_LIST, userId);
        evict(SystemCacheNames.USER_PERMISSION_LIST, userId);
        evict(SystemCacheNames.USER_BY_USERNAME, username);
    }

    public void clearDictCaches() {
        clear(SystemCacheNames.DICT_LIST);
        clear(SystemCacheNames.DICT_ITEMS);
    }

    public void clearDeptCaches() {
        clear(SystemCacheNames.DEPT_LIST);
        clear(SystemCacheNames.DEPT_TREE);
    }

    private void clear(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    private void evict(String cacheName, Object key) {
        if (key == null) {
            return;
        }
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }
}
