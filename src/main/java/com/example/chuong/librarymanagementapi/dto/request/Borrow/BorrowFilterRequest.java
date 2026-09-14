package com.example.chuong.librarymanagementapi.dto.request.Borrow;

import com.example.chuong.librarymanagementapi.entity.Enum.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowFilterRequest {
    /**
     * Lọc theo trạng thái phiếu mượn (BORROWING, RETURNED, OVERDUE...).
     */
    private Status status;
    /**
     * Ngày mượn bắt đầu khoảng lọc (>=). Bắt buộc @DateTimeFormat để Spring
     * parse đúng từ query param dạng "yyyy-MM-dd" (ví dụ fromDate=2024-01-01).
     * Nếu thiếu annotation này, Spring sẽ ném lỗi convert (400) khi client
     * truyền ngày qua query string.
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;
    /**
     * Ngày mượn kết thúc khoảng lọc (<=).
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;

    /**
     * Chỉ dành cho Admin: tìm theo username của người mượn.
     * Service phải bỏ qua field này khi xử lý /my-borrows.
     */
    private String searchUser;
    /**
     * Kiểm tra nhanh khoảng ngày có hợp lệ không (dùng ở Service để validate).
     */
    public boolean isDateRangeInvalid(){
        return fromDate != null && toDate != null && fromDate.isAfter(toDate);
    }

}
