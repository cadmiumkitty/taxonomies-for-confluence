package com.dalstonsemantics.confluence.semantics.cloud.cache;

import java.time.Duration;

import com.atlassian.connect.spring.AtlassianHost;
import com.dalstonsemantics.confluence.semantics.cloud.domain.addon.AddOn;
import com.dalstonsemantics.confluence.semantics.cloud.domain.user.User;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    public static final String HOST_CACHE_NAME = "hostCache";
    public static final String USER_CACHE_NAME = "userCache";
    public static final String ADDON_CACHE_NAME = "addOnCache";

    private CacheManager cacheManager;

    public CacheConfig(
            @Value("${addon.caches.host.heap}") int hostHeap, 
            @Value("${addon.caches.host.ttl-sec}") int hostTtlSec,
            @Value("${addon.caches.user.heap}") int userHeap, 
            @Value("${addon.caches.user.ttl-sec}") int userTtlSec,
            @Value("${addon.caches.addon.heap}") int addOnHeap, 
            @Value("${addon.caches.addon.ttl-sec}") int addOnTtlSec) {

        CacheConfiguration<String, AtlassianHost> hostCacheConfiguration = CacheConfigurationBuilder
            .newCacheConfigurationBuilder(String.class, AtlassianHost.class, ResourcePoolsBuilder.heap(userHeap)) 
            .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(userTtlSec))) 
            .build();
        CacheConfiguration<String, User> userCacheConfiguration = CacheConfigurationBuilder
            .newCacheConfigurationBuilder(String.class, User.class, ResourcePoolsBuilder.heap(userHeap)) 
            .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(userTtlSec))) 
            .build();
        CacheConfiguration<String, AddOn> addOnCacheConfiguration = CacheConfigurationBuilder
            .newCacheConfigurationBuilder(String.class, AddOn.class, ResourcePoolsBuilder.heap(addOnHeap)) 
            .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(addOnTtlSec))) 
            .build();
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
            .withCache(HOST_CACHE_NAME, hostCacheConfiguration) 
            .withCache(USER_CACHE_NAME, userCacheConfiguration) 
            .withCache(ADDON_CACHE_NAME, addOnCacheConfiguration)
            .build(); 
        cacheManager.init(); 
        this.cacheManager = cacheManager;
    }

    @Bean(HOST_CACHE_NAME)
    public Cache<String, AtlassianHost> getHostCache() {
        return this.cacheManager.getCache(HOST_CACHE_NAME, String.class, AtlassianHost.class);
    }

    @Bean(USER_CACHE_NAME)
    public Cache<String, User> getUserCache() {
        return this.cacheManager.getCache(USER_CACHE_NAME, String.class, User.class);
    }

    @Bean(ADDON_CACHE_NAME)
    public Cache<String, AddOn> getAddOnCache() {
        return this.cacheManager.getCache(ADDON_CACHE_NAME, String.class, AddOn.class);
    }
}