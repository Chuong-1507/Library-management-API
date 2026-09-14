package com.example.chuong.librarymanagementapi.controller;

import com.example.chuong.librarymanagementapi.dto.request.Auth.LoginRequest;
import com.example.chuong.librarymanagementapi.dto.response.Auth.LoginResponse;
import com.example.chuong.librarymanagementapi.dto.request.Auth.RegisterRequest;

import com.example.chuong.librarymanagementapi.dto.response.ApiResponse;
import com.example.chuong.librarymanagementapi.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request){

        authService.register(request);

        return ResponseEntity.ok("Register successfully! ");

    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest request){
        LoginResponse loginResponse = authService.login(request);
        ApiResponse<LoginResponse> response = ApiResponse.<LoginResponse>builder()
                .code(200)
                .message("Login successfully!")
                .result(loginResponse)
                .build();
        return ResponseEntity.ok(response);
    }



}
