package com.example.chuong.librarymanagementapi.controller;

import com.example.chuong.librarymanagementapi.config.PaginationUtils;
import com.example.chuong.librarymanagementapi.dto.request.Auth.UserAdminUpdateRequest;
import com.example.chuong.librarymanagementapi.dto.request.Page.PageResponse;
import com.example.chuong.librarymanagementapi.dto.response.ApiResponse;
import com.example.chuong.librarymanagementapi.dto.response.UserResponse;
import com.example.chuong.librarymanagementapi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;

    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> getAllUsers(
            @RequestParam (defaultValue = "1") int page,
            @RequestParam (defaultValue = "10") int size,
            @RequestParam (required = false) String sort
    ){
        Pageable pageable = PaginationUtils.createPageable(page,size,sort, List.of("username","email","fullname","createdAt"),"createdAt");

        Page<UserResponse> result = userService.getAllUsers(pageable);
        return ApiResponse.<PageResponse<UserResponse>>builder()
                .code(200)
                .message("Lấy danh sách người dùng thành công")
                .result(PageResponse.fromPage(result))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable UUID id) {
        UserResponse result = userService.getUserById(id);
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .message("Lấy thông tin người dùng thành công")
                .result(result)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserAdminUpdateRequest request
    ) {
        UserResponse result = userService.updateUserByAdmin(id, request);
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .message("Cập nhật thông tin người dùng thành công")
                .result(result)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable UUID id) {
        userService.softDeleteUser(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Xóa người dùng thành công")
                .build();
    }
}

