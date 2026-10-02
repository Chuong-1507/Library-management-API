package com.example.chuong.librarymanagementapi.service;

import com.example.chuong.librarymanagementapi.dto.request.Auth.ChangePasswordRequest;
import com.example.chuong.librarymanagementapi.dto.request.Auth.UserAdminUpdateRequest;
import com.example.chuong.librarymanagementapi.dto.request.Auth.UserSelfUpdateRequest;
import com.example.chuong.librarymanagementapi.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.UUID;

public interface UserService {
    //ADMIN
    Page<UserResponse> getAllUsers(Pageable pageable);

    UserResponse getUserById(UUID id);

    UserResponse updateUserByAdmin(UUID id, UserAdminUpdateRequest request);

    void softDeleteUser(UUID id);

    //SELF_SERVICE
    UserResponse getMe(String username);

    UserResponse updateMe(String username, UserSelfUpdateRequest request);

    void changePassword(String username, ChangePasswordRequest request);



}
