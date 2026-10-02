package com.example.chuong.librarymanagementapi.controller;

import com.example.chuong.librarymanagementapi.dto.response.*;
import com.example.chuong.librarymanagementapi.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller xử lý các yêu cầu liên quan đến báo cáo, thống kê dành cho Quản trị viên (Admin).
 * Cung cấp các endpoint để thống kê lượt mượn trả, tiền phạt, top sách phổ biến và top độc giả tích cực.
 */
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * Báo cáo thống kê tình hình mượn/trả sách trong một khoảng thời gian.
     *
     * @param from Ngày bắt đầu lọc (định dạng YYYY-MM-DD, không bắt buộc)
     * @param to   Ngày kết thúc lọc (định dạng YYYY-MM-DD, không bắt buộc)
     * @return {@link ApiResponse} chứa thông tin báo cáo mượn trả {@link BorrowReportResponse}
     */
    @GetMapping("/borrows")
    public ApiResponse<BorrowReportResponse> borrowReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.<BorrowReportResponse>builder()
                .result(reportService.getBorrowReport(from, to)).build();
    }

    /**
     * Báo cáo thống kê tiền phạt và các vi phạm mượn trả sách trong một khoảng thời gian.
     *
     * @param from Ngày bắt đầu lọc (định dạng YYYY-MM-DD, không bắt buộc)
     * @param to   Ngày kết thúc lọc (định dạng YYYY-MM-DD, không bắt buộc)
     * @return {@link ApiResponse} chứa thông tin báo cáo tiền phạt {@link FineReportResponse}
     */
    @GetMapping("/fines")
    public ApiResponse<FineReportResponse> fineReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.<FineReportResponse>builder()
                .result(reportService.getFineReport(from, to)).build();
    }

    /**
     * Lấy danh sách những quyển sách được mượn nhiều nhất (sách phổ biến) trong khoảng thời gian.
     *
     * @param from  Ngày bắt đầu lọc (định dạng YYYY-MM-DD, không bắt buộc)
     * @param to    Ngày kết thúc lọc (định dạng YYYY-MM-DD, không bắt buộc)
     * @param limit Số lượng sách tối đa cần lấy (mặc định: 10)
     * @return {@link ApiResponse} chứa danh sách {@link PopularBookResponse}
     */
    @GetMapping("/popular-books")
    public ApiResponse<List<PopularBookResponse>> popularBooks(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.<List<PopularBookResponse>>builder()
                .result(reportService.getPopularBooks(from, to, limit)).build();
    }

    /**
     * Lấy danh sách những người dùng mượn sách nhiều nhất (người dùng tích cực) trong khoảng thời gian.
     *
     * @param from  Ngày bắt đầu lọc (định dạng YYYY-MM-DD, không bắt buộc)
     * @param to    Ngày kết thúc lọc (định dạng YYYY-MM-DD, không bắt buộc)
     * @param limit Số lượng người dùng tối đa cần lấy (mặc định: 10)
     * @return {@link ApiResponse} chứa danh sách {@link ActiveUserResponse}
     */
    @GetMapping("/active-users")
    public ApiResponse<List<ActiveUserResponse>> activeUsers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.<List<ActiveUserResponse>>builder()
                .result(reportService.getActiveUsers(from, to, limit)).build();
    }
}