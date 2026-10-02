package com.example.chuong.librarymanagementapi.dto.request.Auth;

import com.example.chuong.librarymanagementapi.entity.Enum.Role;
import lombok.Data;

import java.util.Set;

@Data
public class UserAdminUpdateRequest {
    private Set<Role> roles;// null = giữ nguyên, không đổi
    private Boolean enabled;// null = giữ nguyên — dùng Boolean (wrapper) chứ không phải boolean để phân biệt "không gửi" với "gửi false"
}
