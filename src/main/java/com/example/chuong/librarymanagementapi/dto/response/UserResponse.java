package com.example.chuong.librarymanagementapi.dto.response;

import com.example.chuong.librarymanagementapi.entity.Enum.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private Set<Role> roles;
    private LocalDateTime createdAt;
}
