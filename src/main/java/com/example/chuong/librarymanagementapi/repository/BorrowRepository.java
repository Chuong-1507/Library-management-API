package com.example.chuong.librarymanagementapi.repository;

import com.example.chuong.librarymanagementapi.dto.response.ActiveUserResponse;
import com.example.chuong.librarymanagementapi.dto.response.PopularBookResponse;
import com.example.chuong.librarymanagementapi.entity.Book;
import com.example.chuong.librarymanagementapi.entity.Borrow;
import com.example.chuong.librarymanagementapi.entity.Enum.Status;
import com.example.chuong.librarymanagementapi.entity.User;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BorrowRepository extends JpaRepository<Borrow, UUID>, JpaSpecificationExecutor<Borrow> {
    /**
     * Lấy danh sách mượn sách theo phân trang theo userId và status
     */
    @EntityGraph(attributePaths = {"user","book"})
    List<Borrow> findByUserId(UUID id, Pageable pageable);

    @EntityGraph(attributePaths = {"user","book"})
    List<Borrow> findByStatus(Status status, Pageable pageable);

    /**
     * Lấy danh sách mượn sách của User kèm thông tin User và Book (dùng EntityGraph tránh N+1 Query).
     */
    @EntityGraph(attributePaths = {"user", "book"})
    List<Borrow> findByUserId(UUID userId);

    /**
     * Lấy danh sách mượn sách của User lọc theo trạng thái cụ thể.
     */
    @EntityGraph(attributePaths = {"user", "book"})
    List<Borrow> findByUserIdAndStatus(UUID userId, Status status);

    /**
     * Lấy toàn bộ phiếu mượn lọc theo trạng thái cụ thể trong hệ thống.
     */
    @EntityGraph(attributePaths = {"user", "book"})
    List<Borrow> findByStatus(Status status);

    /**
     * Lấy tất cả phiếu mượn trong hệ thống cùng với thông tin User và Book.
     */
    @EntityGraph(attributePaths = {"user", "book"})
    @Query("select b from Borrow b")
    List<Borrow> findAllWithUserAndBook();

    /**
     * Tìm phiếu mượn theo ID cùng với thông tin User và Book.
     */
    @EntityGraph(attributePaths = {"user", "book"})
    Optional<Borrow> findById(UUID id);

    /**
     * Đếm số lượng phiếu mượn của User theo 1 trạng thái cụ thể.
     */
    long countByUserIdAndStatus(UUID userId, Status status);

    /**
     * Đếm tổng số phiếu mượn của User nằm trong tập hợp các trạng thái (ví dụ: BORROWING và OVERDUE).
     */
    long countByUserIdAndStatusIn(UUID userId, Collection<Status> statuses);

    /**
     * Kiểm tra người dùng có đang mượn cùng 1 cuốn sách trong tập hợp các trạng thái active hay không.
     */
    boolean existsByUserIdAndBookIdAndStatusIn(UUID userId, UUID bookId, Collection<Status> statuses);

    /**
     * Mục tiêu: Cập nhật đồng loạt (bulk update) trạng thái các phiếu mượn trễ hạn từ BORROWING -> OVERDUE.
     * Cách thức: Chạy 1 câu lệnh UPDATE duy nhất trong DB đối với các bản ghi có returnDate nhỏ hơn ngày hiện tại.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Borrow b SET b.status = :overdueStatus WHERE b.status = :borrowingStatus AND b.returnDate < :today")
    int updateOverdueStatus(@Param("borrowingStatus") Status borrowingStatus,
                            @Param("overdueStatus") Status overdueStatus,
                            @Param("today") LocalDate today);


    List<Borrow> findAllByStatusAndReturnDateBefore(Status status, LocalDate now);

    boolean existsByUserAndBookAndActualReturnDateIsNull(User user, Book book);

    List<Borrow> findAllByStatusAndReturnDate(Status status, LocalDate returnDate);

    @Query("SELECT COUNT(b) FROM Borrow b WHERE (:from IS NULL OR b.borrowDate >= :from) " +
            "AND (:to IS NULL OR b.borrowDate <= :to)")
    long countTotalBorrows(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COUNT(b) FROM Borrow b WHERE b.status = :status AND (:from IS NULL OR b.borrowDate >= :from) " +
            "AND (:to IS NULL OR b.borrowDate <= :to)")
    long countByStatusInRange(@Param("status") Status status, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(b.fineAmount), 0) FROM Borrow b WHERE b.status = 'RETURNED' AND b.fineAmount > 0 " +
    "AND (:from IS NULL OR b.borrowDate >= :from) AND (:to IS NULL OR b.borrowDate <= :to)")
    BigDecimal sumFines(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COUNT(b) FROM Borrow b WHERE b.status = 'RETURNED' AND b.fineAmount > 0 "+
    "AND (:from IS NULL OR b.borrowDate >= :from) AND (:to IS NULL OR b.borrowDate <= :to)")
    long countBorrowsWithFine(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT new com.example.chuong.librarymanagementapi.dto.response.PopularBookResponse(b.book.id, b.book.title, b.book.author, COUNT(b)) " +
            "FROM Borrow b WHERE (:from IS NULL OR b.borrowDate >= :from) AND (:to IS NULL OR b.borrowDate <= :to) " +
            "GROUP BY b.book.id, b.book.title, b.book.author ORDER BY COUNT(b) DESC")
    List<PopularBookResponse> findPopularBooks(@Param("from") LocalDate from, @Param("to") LocalDate to, Pageable pageable);

    @Query("SELECT new com.example.chuong.librarymanagementapi.dto.response.ActiveUserResponse(b.user.id, b.user.username, b.user.fullName, COUNT(b)) " +
            "FROM Borrow b WHERE (:from IS NULL OR b.borrowDate >= :from) AND (:to IS NULL OR b.borrowDate <= :to) " +
            "GROUP BY b.user.id, b.user.username, b.user.fullName ORDER BY COUNT(b) DESC")
    List<ActiveUserResponse> findActiveUsers(@Param("from") LocalDate from, @Param("to") LocalDate to, Pageable pageable);
}
