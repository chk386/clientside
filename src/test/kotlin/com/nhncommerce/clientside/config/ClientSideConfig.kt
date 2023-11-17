package com.nhncommerce.clientside.config

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.lettuce.core.RedisClient
import io.lettuce.core.TrackingArgs
import io.lettuce.core.support.caching.CacheAccessor
import io.lettuce.core.support.caching.ClientSideCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import java.time.Duration
import java.util.HashMap
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function

@Configuration
class ClientSideConfig {

    @Bean
    fun localCache(): Cache<String, String> {
        return
    }

    @Bean
    fun clientSideCaching(connectionFactory: LettuceConnectionFactory, localCache: Cache<String, String>) {
        val redisClient = RedisClient.create(connectionFactory.hostName)
        val connection = redisClient.connect()

        val frontend =
            ClientSideCaching.enable(CacheAccessor.forMap(localCache), connection, TrackingArgs.Builder.enabled())
    }
}


class CaffeineCacheMap: CacheAccessor<String, String>() {
    private val cache: Cache<String, String> = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMillis(1000 * 60 * 60))
        .maximumSize(100_000)
        .build()

    override fun get(key: String): String? {
        return cache.getIfPresent(key)
    }

    override fun put(key: String, value: String): String? {
        return cache.put(key, value)
    }


    override fun evict(key: String?) {
        TODO("Not yet implemented")
    }
}
