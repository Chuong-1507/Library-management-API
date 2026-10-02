package com.example.chuong.librarymanagementapi.repository;

import com.example.chuong.librarymanagementapi.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book,UUID>, JpaSpecificationExecutor<Book> {
    //Tìm sách có title chính xác (VD: tìm SringBoot -> SpringBoot)
    Optional<Book> findByTitleIgnoreCase(String title);
    //Tìm sách có title chứa chuỗi truyền vào (VD: Java -> Java Programming)
    List<Book> findByTitleContainingIgnoreCase(String title);

    @EntityGraph(attributePaths = "category")
    @Query("select b from Book b")
    List<Book> findAllWithCategory();

    /**
     * Mục tiêu: Tìm sách theo ID và kích hoạt Khóa ghi độc quyền (Pessimistic Write Lock / SELECT ... FOR UPDATE).
     * Cách thức: Ngăn chặn các giao dịch (transaction) khác đọc/sửa đổi số lượng sách tồn kho đồng thời, 
     * giải quyết triệt để bài toán Race Condition khi nhiều người cùng mượn hoặc trả cuốn sách này tại cùng một thời điểm.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b where b.id = :id")
    Optional<Book> findByIdWithLock(@Param("id") UUID id);

    @Query(
            value = "SELECT b.* FROM books b " +
                    "WHERE MATCH(b.title, b.author) AGAINST (:keyword IN BOOLEAN MODE) " +
                    "AND b.available_quantity > 0 " +
                    "ORDER BY (SELECT COUNT(*) FROM borrows br WHERE br.book_id = b.id) DESC",
            countQuery = "SELECT COUNT(*) FROM books b " +
                    "WHERE MATCH(b.title, b.author) AGAINST (:keyword IN BOOLEAN MODE) " +
                    "AND b.available_quantity > 0",
            nativeQuery = true
    )
    Page<Book> searchBooksFullText(@Param("keyword") String keyword, Pageable pageable);
}
