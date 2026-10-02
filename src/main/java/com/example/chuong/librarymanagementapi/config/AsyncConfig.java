package com.example.chuong.librarymanagementapi.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
/**
 * Cấu hình xử lý bất đồng bộ (Asynchronous Processing) cho ứng dụng Spring Boot.
 * 
 * - @Configuration: Đánh dấu đây là một lớp cấu hình Spring bean.
 * - @EnableAsync: Kích hoạt tính năng xử lý bất đồng bộ cho các phương thức được đánh dấu @Async.
 * - AsyncConfigurer: Interface cho phép tùy biến ThreadPoolTaskExecutor và bộ xử lý ngoại lệ Async.
 */
@Configuration
@EnableAsync
@Slf4j // tự động tạo một biến log cho class
public class AsyncConfig implements AsyncConfigurer {

    /**
     * Cấu hình ThreadPoolTaskExecutor tùy chỉnh để quản lý và phân bổ thread thực thi các tác vụ bất đồng bộ (@Async).
     *
     * @return Executor - Thread pool đã được khởi tạo và cấu hình
     */
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Số lượng thread cơ bản luôn duy trì hoạt động trong pool
        executor.setCorePoolSize(2);
        
        // Số lượng thread tối đa có thể được tạo khi hàng đợi (queue) bị đầy
        executor.setMaxPoolSize(5);
        
        // Dung lượng hàng đợi chứa các tác vụ đang chờ thực thi trước khi tạo thêm thread (lên tới maxPoolSize)
        executor.setQueueCapacity(50);
        
        // Tiền tố đặt tên cho các thread được tạo ra (dễ theo dõi trong log / debugging)
        executor.setThreadNamePrefix("email-async-");
        
        // Khởi tạo executor với các thông số cấu hình ở trên
        executor.initialize();
        
        return executor;
    }

    /**
     * Bộ xử lý ngoại lệ toàn cục cho các phương thức @Async có kiểu trả về là void (không thể ném exception trực tiếp về caller).
     *
     * @return AsyncUncaughtExceptionHandler - Xử lý log lỗi khi xảy ra ngoại lệ không bắt được trong luồng async
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
                log.error("Lỗi async không bắt được ở method {}: {}", method.getName(), ex.getMessage(), ex);
    }
}
