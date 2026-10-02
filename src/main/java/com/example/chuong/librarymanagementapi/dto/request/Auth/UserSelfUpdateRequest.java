package com.example.chuong.librarymanagementapi.dto.request.Auth;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserSelfUpdateRequest {
    @Email
    private String email;
    private String fullname;
}
