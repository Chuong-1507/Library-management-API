package com.example.chuong.librarymanagementapi.dto.response;

import com.example.chuong.librarymanagementapi.entity.Enum.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowResponse {
    private UUID id;
    private UUID userId;
    private String username;
    private UUID bookId;
    private String bookTitle;
    private LocalDate borrowDate;
    private LocalDate returnDate;
    private LocalDate actualReturnDate;
    private Status status;
    private long overdueDays;
    private BigDecimal fineAmount;
}
