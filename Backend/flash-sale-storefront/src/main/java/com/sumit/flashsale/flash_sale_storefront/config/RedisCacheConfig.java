package com.sumit.flashsale.flash_sale_storefront.config;

import java.time.Duration;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sumit.flashsale.flash_sale_storefront.dto.ProductResponseDto;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
public class RedisCacheConfig {

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
            ObjectMapper objectMapper) {

        var productSerializer =
                new Jackson2JsonRedisSerializer<>(
                        objectMapper, ProductResponseDto.class);

        var flashSaleType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, ProductResponseDto.class);

        var flashSaleSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, flashSaleType);

        var categoriesType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, String.class);

        var categoriesSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, categoriesType);

        RedisCacheConfiguration productConfig = createConfig(productSerializer);
        RedisCacheConfiguration flashSaleConfig = createConfig(flashSaleSerializer);
        RedisCacheConfiguration categoriesConfig = createConfig(categoriesSerializer);

        return builder -> builder
                .withCacheConfiguration("products", productConfig)
                .withCacheConfiguration("flashSaleProducts", flashSaleConfig)
                .withCacheConfiguration("categories", categoriesConfig);
    }

    private RedisCacheConfiguration createConfig(
            org.springframework.data.redis.serializer.RedisSerializer<?> serializer) {

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(4))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(serializer));
    }
}
