package com.example.chuong.librarymanagementapi.controller;

import com.example.chuong.librarymanagementapi.dto.request.Auth.LoginRequest;
import com.example.chuong.librarymanagementapi.dto.request.Auth.RefreshTokenRequest;
import com.example.chuong.librarymanagementapi.dto.response.Auth.LoginResponse;
import com.example.chuong.librarymanagementapi.dto.request.Auth.RegisterRequest;

import com.example.chuong.librarymanagementapi.dto.response.ApiResponse;
import com.example.chuong.librarymanagementapi.service.AuthService;
import com.nimbusds.oauth2.sdk.ciba.AuthRequestID;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Ref;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Object> registerUser(@Valid @RequestBody RegisterRequest request) {
          return ApiResponse.builder()
                .code(201)
                .message("Register successfully !")
                .result(authService.register(request))
                .build();

    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> loginUser(@Valid @RequestBody LoginRequest request){
        LoginResponse loginResponse = authService.login(request);
        return ApiResponse.<LoginResponse>builder()
                .code(200)
                .message("Login successfully!")
                .result(loginResponse)
                .build();
        
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@RequestBody @Valid RefreshTokenRequest request){
        return ApiResponse.<LoginResponse> builder()
                .code(201)
                .message("Created new AccessToken by RefreshToken Successfully !")
                .result(authService.refresh(request))
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody @Valid RefreshTokenRequest request){
        authService.logout(request);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Log out successfully !").build();
    }


}
