package com.example.chuong.librarymanagementapi.dto.request.Borrow;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowCreateRequest {
    @NotNull(message = "bookId không được để trống")
    private UUID bookId;

    @NotNull(message = "borrowDate không được để trống")
    private LocalDate borrowDate;

    @NotNull(message = "returnDate không được để trống")
    @FutureOrPresent(message = "returnDate phải là ngày ở hiện tại hoặc tương lai")
    private LocalDate returnDate;
}
