package com.example.chuong.librarymanagementapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(
        name = "books",
        indexes = {
                @Index(name = "idx_book_title", columnList = "title"),
                @Index(name = "idx_book_author", columnList = "author"),
                @Index(name = "idx_book_category_id", columnList = "category_id"),
                @Index(name = "idx_book_price", columnList = "price")
        }
)
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull //chặn null từ validate của API (kiểm tra bằng @Valid)
    @Column(unique = true, nullable = false) // chặn null từ phía database
    private String title;

    @NotNull
    @Column(nullable = false)
    private String author;


    private String publisher;

    private Integer publicationYear;

    @NotNull
    @Column(nullable = false)
    @DecimalMin(value = "0.0", message = "Không được âm")
    private BigDecimal price;

    @NotNull
    @Column(nullable = false)
    @Min(value = 0, message = "Không được âm")
    //Số lượng sách có thể mượn được ngay
    private Integer availableQuantity;

    @NotNull
    @Column(nullable = false)
    @Min(value = 0, message = "Không được âm")
    //total_quantity = available_quantity + quantity;
    //Không đổi khi mượn/trả
    private Integer totalQuantity;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties("books")// khi serialize category, bỏ qua books của từng category (tránh vòng lặp)
    private Category category;

    @Column(updatable = false)
    private LocalDate createdAt;

}
