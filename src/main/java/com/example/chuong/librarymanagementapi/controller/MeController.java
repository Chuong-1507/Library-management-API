package com.example.chuong.librarymanagementapi.controller;

import com.example.chuong.librarymanagementapi.dto.request.Auth.ChangePasswordRequest;
import com.example.chuong.librarymanagementapi.dto.request.Auth.UserSelfUpdateRequest;
import com.example.chuong.librarymanagementapi.dto.response.ApiResponse;
import com.example.chuong.librarymanagementapi.dto.response.UserResponse;
import com.example.chuong.librarymanagementapi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class MeController {
    private final UserService userService;

    @GetMapping
    public ApiResponse<UserResponse> getMe (Authentication authentication){
        return ApiResponse.<UserResponse> builder()
                .code(200)
                .message("Lấy thông tin thành công !")
                .result(userService.getMe(authentication.getName()))
                .build();
    }

    @PutMapping
    public ApiResponse<UserResponse> updateMe(Authentication authentication,
                                              @RequestBody @Valid UserSelfUpdateRequest request){
        return ApiResponse.<UserResponse> builder()
                .code(200)
                .message("Thay đổi email và fullname thành công !")
                .result(userService.updateMe(authentication.getName(),request))
                .build();
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(Authentication authentication,
                                            @RequestBody @Valid ChangePasswordRequest request
                                            ){
        userService.changePassword(authentication.getName(),request);
        return ApiResponse.<Void> builder()
                .code(200)
                .message("Đổi mật khẩu thành công !")
                .build();
    }
}
