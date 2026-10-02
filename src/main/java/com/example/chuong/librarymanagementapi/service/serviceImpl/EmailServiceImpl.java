package com.example.chuong.librarymanagementapi.service.serviceImpl;

import com.example.chuong.librarymanagementapi.entity.Book;
import com.example.chuong.librarymanagementapi.entity.User;
import com.example.chuong.librarymanagementapi.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Khi mượn/trả sách -> hệ tống có thể gửi email thông báo cho user*/
@Service
@RequiredArgsConstructor
@Slf4j // tự động tạo biến logger
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username")
    private String fromAddress;

    /**
     * Gửi email xác nhận mượn sách thành công*/
    @Async
    @Override
    public void sendBorrowConfirmation(User user, Book book, LocalDate returnDate) {
        if (user.getEmail() == null) return; //user cũ chưa có email - thoát method, không throw
        send(user.getEmail(),"Xác nhận mượn sách",
                "Bạn đã mượn thành công \"%s\". Hạn trả: %s.".formatted(book.getTitle(),returnDate));
    }

    @Async
    @Override
    public void sendDueDateReminder(User user, Book book, LocalDate returnDate) {
        if (user.getEmail() == null) return;
        send(user.getEmail(), "Nhắc hạn trả sách",
                "Sách \"%s\" sẽ đến hạn trả vào ngày %s.".formatted(book.getTitle(),returnDate));
    }

    @Async
    @Override
    public void sendFineNotification(User user, Book book, BigDecimal fineAmount) {
        if (user.getEmail() == null) return;
        send(user.getEmail(), "Thông báo phạt trả sách trễ",
                "Bạn trả sách \"%s\" trễ hạn, số tiền phạt: %s VND".formatted(book.getTitle(),fineAmount));
    }

    private void send(String to, String subject, String body){
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        }catch (MailException exception){
            //Không throw lại - lỗi gửi mail (SMTP sập, timeout ...)
            log.error("Gửi email thất bại tới {}: {}",to,exception.getMessage());
        }
    }
}
