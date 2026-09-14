package com.example.chuong.librarymanagementapi.service;

import com.example.chuong.librarymanagementapi.dto.request.Auth.LoginRequest;
import com.example.chuong.librarymanagementapi.dto.response.Auth.LoginResponse;
import com.example.chuong.librarymanagementapi.dto.request.Auth.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}
