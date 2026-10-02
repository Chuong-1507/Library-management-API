package com.example.chuong.librarymanagementapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PopularBookResponse {
    private UUID bookId;
    private String title;
    private String author;
    private Long borrowCount; // Long (Wrapper) - Khớp kiểu trả về của COUNT() trong JPQL
}
