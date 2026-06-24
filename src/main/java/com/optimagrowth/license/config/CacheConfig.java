package com.optimagrowth.license.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/**
 * Configures a Redis-backed distributed cache for the licensing service.
 * <p>
 * Why Redis instead of the default ConcurrentMapCache? <br>
 * Spring's default cache is in-memory and per-JVM. When this service scales
 * to multiple instances, each instance holds its own isolated copy of cached
 * organization data with no shared invalidation. Redis provides a single
 * shared cache that all instances read from and write to, and it survives
 * service restarts.
 * </p>
 * Implements CachingConfigurer to register a custom error handler that
 * prevents a Redis outage from cascading into application failures.
 */
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    private static final Logger LOG = LoggerFactory.getLogger(CacheConfig.class);

    /**
     * Shared TTL and serialization settings applied to all cache regions
     * unless overridden per-cache.
     * <p>
     * TTL of 30 minutes is a safe default for organization data that rarely
     * changes. Events from the organization service (via Kafka) will trigger
     * early eviction whenever a real change occurs, so the TTL is only the
     * last line of defense against stale data — not the primary mechanism.
     * </p><p>
     * GenericJackson2JsonRedisSerializer stores JSON in Redis, which makes
     * inspecting cache entries with redis-cli straightforward during debugging.
     * </p>
     * disableCachingNullValues() prevents a "not found" response from being
     * stored and silently served to all future callers for up to 30 minutes.
     */
    @Bean
    public RedisCacheConfiguration defaultCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()
                        )
                )
                .disableCachingNullValues();
    }

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory factory,
            RedisCacheConfiguration defaultCacheConfiguration) {

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultCacheConfiguration)
                .build();
    }

    /**
     * Prevents Redis errors from propagating up as HTTP 500s.
     * <p>
     * On a GET failure the request continues without a cache hit — the caller
     * falls through to the organization service as if the cache were empty.
     * On a PUT failure the response is still returned to the caller; it is
     * just not stored, so the next request will incur another HTTP call.
     * On an EVICT failure the stale entry may persist until TTL expiry. The
     * warning log makes this visible so on-call can react if needed.
     * </p>
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {

            @Override
            public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
                LOG.warn("Redis GET failed — proceeding without cache. cache={}, key={}, error={}",
                        cache.getName(), key, e.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
                LOG.warn("Redis PUT failed — response returned but not cached. cache={}, key={}, error={}",
                        cache.getName(), key, e.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
                LOG.warn("Redis EVICT failed — stale entry may persist until TTL expiry. cache={}, key={}, error={}",
                        cache.getName(), key, e.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException e, Cache cache) {
                LOG.warn("Redis CLEAR failed. cache={}, error={}", cache.getName(), e.getMessage());
            }
        };
    }
}
