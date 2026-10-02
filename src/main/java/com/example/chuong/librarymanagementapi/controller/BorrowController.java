package com.example.chuong.librarymanagementapi.controller;

import com.example.chuong.librarymanagementapi.dto.request.Borrow.BorrowCreateRequest;
import com.example.chuong.librarymanagementapi.dto.request.Borrow.BorrowFilterRequest;
import com.example.chuong.librarymanagementapi.dto.request.Page.PageResponse;
import com.example.chuong.librarymanagementapi.dto.response.ApiResponse;
import com.example.chuong.librarymanagementapi.dto.response.BorrowResponse;
import com.example.chuong.librarymanagementapi.service.BorrowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;


    /**
     * Lấy danh sách phiếu mượn của CHÍNH user đang đăng nhập.
     *
     * Authentication được Spring Security tự inject từ SecurityContext của
     * request hiện tại — không cần và không được nhận userId/username qua
     * query param cho endpoint này.
     */
    @GetMapping("/my-borrows")
    public ApiResponse<PageResponse<BorrowResponse>> getMyBorrowsAuthenticated(
            Authentication authentication,
            @ModelAttribute BorrowFilterRequest filterRequest,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "borrowDate,desc") String sort
            ){
        PageResponse<BorrowResponse> result =
                borrowService.getMyBorrows(authentication,filterRequest,page,size,sort);
        return ApiResponse.<PageResponse<BorrowResponse>> builder()
                .code(200)
                .message("Lấy danh sách phiếu mượn của bạn thành công")
                .result(result)
                .build();
    }
    /**
     * Admin API: lấy toàn bộ phiếu mượn, có thể lọc theo searchUser.
     * Ví dụ: GET /api/borrows?page=1&size=10&status=OVERDUE
     *
     * Chỉ role ADMIN mới được gọi — chặn bằng @PreAuthorize (yêu cầu đã bật
     * @EnableMethodSecurity trong SecurityConfig của project).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponse<PageResponse<BorrowResponse>> getAllBorrows(
            @ModelAttribute BorrowFilterRequest filterRequest,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "borrowDate,desc") String sort
    ){
        PageResponse<BorrowResponse> result = borrowService.getAllBorrows(filterRequest,page,size,sort);

        return ApiResponse.<PageResponse<BorrowResponse>> builder()
                .code(200)
                .message("Lấy toàn bộ danh sách phiếu mượn thành công !")
                .result(result)
                .build();
    }

    /**
     * Mục tiêu: API tiếp nhận yêu cầu mượn sách từ người dùng đã đăng nhập.
     * 
     * Cách thức hoạt động:
     * - Endpoint: POST /api/borrows
     * - Lấy `username` từ đối tượng `Authentication` (Security Context).
     * - Validate payload bằng `@Valid` và gọi `borrowService.createBorrow(...)`.
     * - Trả về `ApiResponse` bọc `BorrowResponse` thành công với HTTP status 200 OK.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BorrowResponse> createBorrow(
            @Valid @RequestBody BorrowCreateRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        BorrowResponse borrowResponse = borrowService.createBorrow(username, request);

        return ApiResponse.<BorrowResponse>builder()
                .code(201)
                .message("Đăng ký mượn sách thành công")
                .result(borrowResponse)
                .build();
    }

    /**
     * Mục tiêu: API xử lý trả sách và tính tiền phạt cho một phiếu mượn cụ thể.
     * 
     * Cách thức hoạt động:
     * - Endpoint: PUT /api/borrows/{id}/return
     * - Trích xuất `username` và role `ROLE_ADMIN` từ `Authentication`.
     * - Gọi `borrowService.returnBook(...)` để thực hiện logic nghiệp vụ trả sách và tính phạt.
     * - Trả về `ApiResponse` kết quả trả sách thành công.
     */
    @PutMapping("/{id}/return")
    public ApiResponse<BorrowResponse> returnBook(
            @PathVariable UUID id,
            Authentication authentication) {
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        BorrowResponse borrowResponse = borrowService.returnBook(id, username, isAdmin);

        return ApiResponse.<BorrowResponse>builder()
                .code(200)
                .message("Trả sách thành công")
                .result(borrowResponse)
                .build();
    }
}
