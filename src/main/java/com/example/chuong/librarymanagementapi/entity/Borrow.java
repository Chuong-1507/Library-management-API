package com.example.chuong.librarymanagementapi.entity;

import com.example.chuong.librarymanagementapi.entity.Enum.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NotNull
@Table(name = "borrows",
        indexes = {
                @Index(name = "idx_borrow_user_id",columnList = "user_id"),
                @Index(name = "idx_borrow_book_id", columnList = "book_id"),
                @Index(name = "idx_borrow_status", columnList = "status"),
                @Index(name = "idx_borrow_date", columnList = "borrow_date")
        }
)
public class Borrow {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id",nullable = false)
    private Book book;

    @Column(name = "borrow_date", nullable = false)
    private LocalDate borrowDate;

    @Column(nullable = false)
    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false)
    private Status status;

    private LocalDate actualReturnDate;

    @Column(name = "fine_amount",precision = 10, scale = 2)
    private BigDecimal fineAmount;
}
