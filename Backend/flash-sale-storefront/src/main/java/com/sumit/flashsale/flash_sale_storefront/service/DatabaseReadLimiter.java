package com.sumit.flashsale.flash_sale_storefront.service;


import java.util.List;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class DatabaseReadLimiter {

    private static final String KEY = "db:product-reads:limit";
    private static final String ALLOWED_KEY = "db:product-reads:allowed";
    private static final String REJECTED_KEY = "db:product-reads:rejected";
    private static final int MAX_READS_PER_SECOND = 25;

    private static final DefaultRedisScript<Long> SCRIPT =
            new DefaultRedisScript<>("""
                local time = redis.call('TIME')
                local now = time[1] * 1000 + math.floor(time[2] / 1000)

                redis.call('ZREMRANGEBYSCORE', KEYS[1], '-inf', now - 1000)
                local count = redis.call('ZCARD', KEYS[1])

                if count >= tonumber(ARGV[1]) then
                    redis.call('INCR', KEYS[3])
                    return 0
                end

                redis.call('ZADD', KEYS[1], now, ARGV[2])
                redis.call('PEXPIRE', KEYS[1], 2000)
                redis.call('INCR', KEYS[2])
                return 1
                """, Long.class);

    private final StringRedisTemplate redisTemplate;

    public DatabaseReadLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void acquirePermit() {
        Long allowed = redisTemplate.execute(
                SCRIPT,
                List.of(KEY, ALLOWED_KEY, REJECTED_KEY),
                String.valueOf(MAX_READS_PER_SECOND),
                UUID.randomUUID().toString()
        );

        if (allowed == null || allowed != 1L) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Database read capacity reached. Please retry shortly."
            );
        }
    }

    public long getAllowedCount() {
        return readCounter(ALLOWED_KEY);
    }

    public long getRejectedCount() {
        return readCounter(REJECTED_KEY);
    }

    private long readCounter(String key) {
        String value = redisTemplate.opsForValue().get(key);
        return value == null ? 0L : Long.parseLong(value);
    }
}
