package com.example.chuong.librarymanagementapi.dto.request;

import com.example.chuong.librarymanagementapi.entity.Category;
import jakarta.validation.constraints.*;
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
public class BookCreateRequest {
    @NotBlank(message = "Title không được trống")
    @Size(min = 0)
    private String title;

    @NotBlank(message = "Author không được trống")
    private String author;

//    @NotNull(message = "Price không được null")
    @DecimalMin(value = "0.0",message = "Price không được âm")
    private BigDecimal price;

    @NotNull(message = "Quantity không được null")
    @Min(value = 0, message = "Quantity không được âm")
    private Integer totalQuantity;

    @NotNull(message = "Category không được null")
    private UUID categoryId;

//    @NotNull(message = "publisher không được null")
    private String publisher;

//    @NotNull(message = "pulicationYear không được null")
    private Integer publicationYear;

    private LocalDate createdAt;
}
