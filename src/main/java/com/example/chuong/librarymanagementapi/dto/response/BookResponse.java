package com.example.chuong.librarymanagementapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {
    private UUID id;
    private String title;
    private String author;
    private BigDecimal price;
    private Integer quantity;
    private String categoryName;
    private String publisher;
    private Integer publicationYear;
    private LocalDate createdAt;

}
