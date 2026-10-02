package com.example.chuong.librarymanagementapi.service;

import com.example.chuong.librarymanagementapi.dto.request.Borrow.BorrowCreateRequest;
import com.example.chuong.librarymanagementapi.entity.Book;
import com.example.chuong.librarymanagementapi.entity.User;
import com.example.chuong.librarymanagementapi.exception.AppException;
import com.example.chuong.librarymanagementapi.repository.BookRepository;
import com.example.chuong.librarymanagementapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest //Khởi động toàn bộ Spring Boot Application Context để chạy test
@ActiveProfiles("integration-test")
public class BorrowServiceIntegrationTest {
    @Container
    // Tạo một container MySQL
    // sử dụng Docker image MySQL phiên bản 8.0
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("library_test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry){
        // Đăng ký các cấu hình database cho Spring Boot khi chạy test.
        // Vì MySQL được tạo động bởi Testcontainers nên URL, username, password
        // không cố định → lấy trực tiếp từ MySQL container.
        registry.add("spring.datasource.url",mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username",mySQLContainer::getUsername);
        registry.add("spring.datasource.password",mySQLContainer::getPassword);
    }

    @Autowired private BorrowService borrowService;
    @Autowired private BookRepository bookRepository;
    @Autowired private UserRepository userRepository;

    @Test
        //khi nhiều request mượn đồng thời, chỉ nhận một request (tránh race condition)
    void createBorrow_concurrentCheckout_onlyOneSucceeds() throws InterruptedException {
        Book book = bookRepository.save(Book.builder()
                .title("Concurrency-" + UUID.randomUUID())
                .author("Ẩn danh")
                .price(BigDecimal.valueOf(1240555))
                .totalQuantity(1)
                .availableQuantity(1)
                .build());
        User userA = userRepository.save(User.builder().username("userA"+UUID.randomUUID().toString()).password("123456").build());
        User userB = userRepository.save(User.builder().username("userB"+UUID.randomUUID().toString()).password("123456").build());

        //Tạo thread pool gồm 2 thread để mô phỏng
        //2 user cùng lúc thực hiện thao tác mượn sách
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        //Tạo "cổng đồng bộ" (rào cản) với count = 1
        //Hai thread sẽ chờ tại start.await()
        //Khi thread chính gọi start.countDown(), cả 2 thread được phép tiếp tục
        CountDownLatch start = new CountDownLatch(1);

        //Tạo danh sách gồm 2 task
        //Mỗi task trả về một giá trị Boolean: true,false
        List<Callable<Boolean>> tasks = List.of(
                () -> {
                    //Chờ cho đến khi thread được gọi start.countDown().
                    start.await();
                    // Sau khi "cổng" mở, User A thực hiện mượn sách.
                    return tryBorrow(userA.getUsername(), book.getId());
                },
                () -> {
                    // Tương tự User A, User B cũng chờ tín hiệu bắt đầu.
                    start.await();
                    // Sau khi "cổng" mở, User B thực hiện mượn cùng cuốn sách.
                    return tryBorrow(userB.getUsername(), book.getId());
                }
        );
        //Mở "cổng" cho 2 task vào chạy cùng lúc
        start.countDown();
        //Gửi 2 task vào ExecutorService để chạy theo 2 thread (đã khởi tạo ở trên) đồng thời
        //invokeAll() trả về danh sách Future tương ứng kết quả của 2 task
        List<Future<Boolean>> result = executorService.invokeAll(tasks);

        //Không nhận thêm task mới, các task được submit tiếp tục hoàn thành
        executorService.shutdown();

        //Chờ tối đa 5s để các thread hoàn thành
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        //Đếm xem có bao nhiêu task trả về true trong danh sách Future
        long successCount = result.stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .filter(Boolean::booleanValue)
                .count();

        //Mong muốn chỉ 1 user có thể mượn thành công
        assertThat(successCount).isEqualTo(1);
        //Kiểm tra database sau khi 1 user mượn thành công
        //availableQuantity ban đầu = 1 -> 0
        assertThat(bookRepository.findById(book.getId()).orElseThrow().getAvailableQuantity()).isEqualTo(0);
    }

    private boolean tryBorrow(String username, UUID bookId){
        try {
            BorrowCreateRequest request = BorrowCreateRequest.builder()
                            .bookId(bookId)
                            .borrowDate(LocalDate.now())
                            .returnDate(LocalDate.now().plusDays(7))
                            .build();
            borrowService.createBorrow(username, request);
            return true;
        } catch (Exception e){
            return false;
        }
    }
}
