package com.example.chuong.librarymanagementapi.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BorrowReportResponse {
    private long totalBorrows;
    private long currentlyBorrowed;
    private long overdue;
    private long returned;
}
