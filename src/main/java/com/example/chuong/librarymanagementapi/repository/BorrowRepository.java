package com.example.chuong.librarymanagementapi.repository;

import com.example.chuong.librarymanagementapi.entity.Borrow;
import com.example.chuong.librarymanagementapi.entity.Enum.Status;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
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



}
