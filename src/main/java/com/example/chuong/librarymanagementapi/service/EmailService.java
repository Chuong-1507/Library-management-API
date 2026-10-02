package com.example.chuong.librarymanagementapi.service;

import com.example.chuong.librarymanagementapi.entity.Book;
import com.example.chuong.librarymanagementapi.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface EmailService {
    void sendBorrowConfirmation(User user, Book book, LocalDate returnDate);
    void sendDueDateReminder(User user, Book book, LocalDate returnDate);
    void sendFineNotification(User user, Book book, BigDecimal fineAmount);
}
