package com.example.chuong.librarymanagementapi.entity;

import com.example.chuong.librarymanagementapi.entity.Enum.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
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

    private Double fineAmount;
}
