package com.example.chuong.librarymanagementapi.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;

/**
 * =========================================================================================
 * MỤC TIÊU CỦA CLASS {@link RedisConfig}:
 * 
 * 1. Tích hợp Redis làm hệ thống Distributed Cache (Bộ nhớ đệm phân tán) cho toàn bộ ứng dụng.
 * 2. Tăng hiệu năng (Performance) và giảm tải cho Database (MySQL) đối với các truy vấn đọc dữ liệu thường xuyên.
 * 3. Chuẩn hóa cấu hình Cache:
 *    - Định dạng lưu trữ: Chuyển đổi dữ liệu đối tượng Java sang JSON để tương thích và dễ debug.
 *    - Thời gian hết hạn (TTL): Tự động dọn dẹp dữ liệu cũ (10 phút) để tránh tràn bộ nhớ.
 *    - Tối ưu bộ nhớ: Bỏ qua không cache dữ liệu null.
 * 4. Cung cấp Bean {@link RedisCacheManager} để Spring quản lý tự động thông qua các annotation
 *    như {@code @Cacheable}, {@code @CachePut}, {@code @CacheEvict} trong tầng Service.
 * =========================================================================================
 */
@Configuration
@EnableCaching // Kích hoạt tính năng Caching của Spring Boot (cho phép sử dụng @Cacheable, @CachePut, @CacheEvict,...)
public class RedisConfig {

    /**
     * Cấu hình chi tiết cho từng entry trong Redis Cache:
     * - TTL (Time to Live): Thời gian sống mặc định của cache là 10 phút.
     * - Serialization: Sử dụng JSON serializer để dữ liệu lưu vào Redis ở dạng JSON dễ đọc/debug thay vì binary Java mặc định.
     * - disableCachingNullValues: Không lưu các giá trị null vào cache để tránh lãng phí bộ nhớ.
     */
    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                // Thiết lập thời gian hết hạn của cache (10 phút)
                .entryTtl(Duration.ofMinutes(10))
                // Cấu hình serialize key sang String (dễ đọc trên Redis CLI / GUI)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
                // Cấu hình serialize giá trị (Value) sang định dạng JSON
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()))
                // Không cache các giá trị trả về là null
                .disableCachingNullValues();
                
    }

    /**
     * Khởi tạo CacheManager để Spring quản lý toàn bộ vòng đời của Cache thông qua Redis.
     * 
     * @param connectionFactory được Spring Boot tự động inject dựa trên cấu hình host/port trong application.properties
     * @return RedisCacheManager áp dụng cấu hình mặc định từ cacheConfiguration()
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration())
                .build();
    }
}
