package com.example.chuong.librarymanagementapi.scheduler;

import com.example.chuong.librarymanagementapi.entity.Borrow;
import com.example.chuong.librarymanagementapi.entity.Enum.Status;
import com.example.chuong.librarymanagementapi.repository.BorrowRepository;
import com.example.chuong.librarymanagementapi.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DueDateReminderJob {
    private final BorrowRepository borrowRepository;
    private final EmailService emailService;

    /**Mỗi ngày lúc 8:00 sáng → tìm những người có sách đến hạn trả vào ngày mai → gửi email nhắc hạn trả.*/
    @Scheduled(cron = " 0 0 8 * * *") // 8h sáng mỗi ngày
    public void remindDueSoon(){
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Borrow> dueSoon = borrowRepository.findAllByStatusAndReturnDateBefore(Status.BORROWING,tomorrow);

        // duyệt qua từng Borrow trong danh sách và gửi email cho từng người
        dueSoon.forEach(b -> emailService.sendDueDateReminder(b.getUser(),b.getBook(),b.getReturnDate()));
    }
}
