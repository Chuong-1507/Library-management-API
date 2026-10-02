package com.example.chuong.librarymanagementapi.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filter dùng để giới hạn số lần yêu cầu đăng nhập (Rate Limiting) trên từng địa chỉ IP của Client.
 * Sử dụng thư viện Bucket4j theo thuật toán Token Bucket nhằm chống tấn công Brute-force mật khẩu.
 * Kế thừa OncePerRequestFilter để đảm bảo filter chỉ thực thi một lần duy nhất cho mỗi HTTP request.
 */
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    /**
     * Endpoint API đăng nhập cần áp dụng giới hạn request.
     */
    private static final String LOGIN_PATH = "/api/auth/login";

    /**
     * Bộ nhớ tạm (In-memory Cache) lưu trữ Bucket tương ứng với từng địa chỉ IP.
     * Sử dụng ConcurrentHashMap để đảm bảo tính an toàn đa luồng (Thread-safe) khi có nhiều request đồng thời.
     */
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Xử lý kiểm tra và áp dụng Rate Limiting trước khi request đến được Controller.
     *
     * @param request     HTTP request từ client gửi lên
     * @param response    HTTP response trả về cho client
     * @param filterChain Chuỗi các filter tiếp theo trong Spring Security
     * @throws ServletException nếu có lỗi servlet
     * @throws IOException      nếu có lỗi I/O
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. Kiểm tra: Chỉ áp dụng Rate Limit cho request POST đến endpoint đăng nhập (/api/auth/login).
        // Các request khác (GET, endpoint khác, v.v.) sẽ được bỏ qua và tiếp tục chuỗi filter bình thường.
        if (!("POST".equalsIgnoreCase(request.getMethod()) && LOGIN_PATH.equals(request.getRequestURI()))) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Lấy hoặc khởi tạo Bucket (Token Bucket) cho IP của client hiện tại.
        // computeIfAbsent: nếu IP chưa có trong Map thì tạo Bucket mới thông qua hàm newBucket().
        Bucket bucket = buckets.computeIfAbsent(extractClientIp(request), ip -> newBucket());

        // 3. Thử tiêu thụ 1 token từ Bucket.
        if (bucket.tryConsume(1)) {
            // Còn token khả dụng -> Cho phép request đi tiếp vào Controller xử lý đăng nhập
            filterChain.doFilter(request, response);
        } else {
            // Hết token (vượt quá số lần cho phép) -> Chặn request và trả về lỗi HTTP 429 Too Many Requests
            response.setStatus(429); // Mã trạng thái 429: Too Many Requests
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":429,\"message\":\"Quá nhiều lần đăng nhập, thử lại sau ít phút\"}");
        }
    }

    /**
     * Tạo một Bucket mới với cấu hình giới hạn số lượng request.
     * 
     * Cấu hình:
     * - Dung lượng tối đa (Capacity): 5 tokens (tối đa 5 lần thử đăng nhập).
     * - Cơ chế hồi phục (Refill): Hồi phục lại 5 tokens sau mỗi chu kỳ 5 phút.
     *
     * @return Bucket mới được cấu hình theo thuật toán Token Bucket
     */
    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(5)
                .refillIntervally(5, Duration.ofMinutes(5))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Lấy địa chỉ IP thật của client từ HTTP request.
     * Trường hợp ứng dụng chạy sau Proxy/Load Balancer (Nginx, Cloudflare, v.v.),
     * IP gốc sẽ nằm trong header "X-Forwarded-For".
     *
     * @param request HTTP request
     * @return Địa chỉ IP của client
     */
    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");

        return (forwarded != null && !forwarded.isBlank())
                ? forwarded.split(",")[0].trim()
                : request.getRemoteAddr();
    }
}


