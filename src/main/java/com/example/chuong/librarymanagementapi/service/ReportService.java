package com.example.chuong.librarymanagementapi.service;

import com.example.chuong.librarymanagementapi.dto.response.ActiveUserResponse;
import com.example.chuong.librarymanagementapi.dto.response.BorrowReportResponse;
import com.example.chuong.librarymanagementapi.dto.response.FineReportResponse;
import com.example.chuong.librarymanagementapi.dto.response.PopularBookResponse;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    BorrowReportResponse getBorrowReport(LocalDate from, LocalDate to);
    FineReportResponse getFineReport(LocalDate from, LocalDate to);
    List<PopularBookResponse> getPopularBooks (LocalDate from, LocalDate to, int limit);
    List<ActiveUserResponse> getActiveUsers(LocalDate from, LocalDate to, int limit);
}
