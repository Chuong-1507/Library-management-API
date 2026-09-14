package com.example.chuong.librarymanagementapi.scheduler;

import com.example.chuong.librarymanagementapi.service.BorrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BorrowScheduler {

    private final BorrowService borrowService;

    /**
     * Mục tiêu: Tự động kiểm tra và cập nhật trạng thái của các phiếu mượn đã quá hạn từ BORROWING sang OVERDUE trong database.
     * 
     * Cách thức hoạt động:
     * - Được kích hoạt tự động theo chu kỳ (Cron Expression: "0 0 * * * ?" - chạy vào phút 00 của mỗi giờ).
     * - Gọi service method borrowService.updateOverdueBorrows() thực thi bulk update SQL để chuyển đổi trạng thái trực tiếp trong DB.
     * - Ghi log số lượng bản ghi phiếu mượn đã được cập nhật thành công sang OVERDUE.
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void scheduleOverdueUpdate() {
        log.info("Running scheduled task: update overdue borrows in database");
        int updatedCount = borrowService.updateOverdueBorrows();
        log.info("Completed scheduled task: {} borrow record(s) updated to OVERDUE", updatedCount);
    }
}
