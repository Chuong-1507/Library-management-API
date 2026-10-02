package com.example.chuong.librarymanagementapi.mapper;

import com.example.chuong.librarymanagementapi.dto.response.UserResponse;
import com.example.chuong.librarymanagementapi.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);
}
