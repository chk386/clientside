package com.nhncommerce.clientside.config

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.lettuce.core.RedisChannelHandler
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisConnectionStateListener
import io.lettuce.core.TrackingArgs
import io.lettuce.core.support.caching.CacheAccessor
import io.lettuce.core.support.caching.CacheFrontend
import io.lettuce.core.support.caching.ClientSideCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import java.net.SocketAddress
import java.time.Duration

@Configuration
class ClientSideConfig {

    @Bean
    fun localCacheMap() = CaffeineCacheMap()

    @Bean
    fun clientSideCaching(
        connectionFactory: LettuceConnectionFactory,
        localCacheMap: CaffeineCacheMap,
    ): CacheFrontend<String, String>? {
        val redisClient = RedisClient.create(connectionFactory.hostName)
        val connection = redisClient.connect()

        redisClient.addListener(object: RedisConnectionStateListener {
            override fun onRedisConnected(connection: RedisChannelHandler<*, *>?, socketAddress: SocketAddress?) {
                super.onRedisConnected(connection, socketAddress)
            }

            override fun onRedisDisconnected(connection: RedisChannelHandler<*, *>?) {
                super.onRedisDisconnected(connection)
            }

            override fun onRedisExceptionCaught(connection: RedisChannelHandler<*, *>?, cause: Throwable?) {
                super.onRedisExceptionCaught(connection, cause)
            }
        })


        val cacheFrontend = ClientSideCaching.enable(localCacheMap, connection, TrackingArgs.Builder.enabled())
        return enable
    }

    @Bean
    fun redisTemplate(connectionFactory: LettuceConnectionFactory): ReactiveStringRedisTemplate {
        return ReactiveStringRedisTemplate(connectionFactory)
    }
}

class CaffeineCacheMap : CacheAccessor<String, String> {
    private val cache: Cache<String, String> = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMillis(1000 * 60 * 60))
        .maximumSize(100_000)
        .build()

    override fun get(key: String): String? {
        return cache.getIfPresent(key)
    }

    override fun put(key: String, value: String) {
        return cache.put(key, value)
    }

    override fun evict(key: String?) {
        return cache.invalidate(key)
    }
}
