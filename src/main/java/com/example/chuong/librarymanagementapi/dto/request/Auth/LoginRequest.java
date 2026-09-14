package com.example.chuong.librarymanagementapi.dto.request.Auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Username không được để trống")
    private final String username;

    @NotBlank(message = "Password không được để trống")
    private final String password;
}
