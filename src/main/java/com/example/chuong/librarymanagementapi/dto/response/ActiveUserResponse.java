package com.example.chuong.librarymanagementapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ActiveUserResponse {
    private UUID userId;
    private String username;
    private String fullName;
    private Long borrowCount;
}
