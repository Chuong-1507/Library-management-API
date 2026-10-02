package com.example.chuong.librarymanagementapi.repository;

import com.example.chuong.librarymanagementapi.entity.Book;
import com.example.chuong.librarymanagementapi.entity.Borrow;
import com.example.chuong.librarymanagementapi.entity.Enum.Role;
import com.example.chuong.librarymanagementapi.entity.Enum.Status;
import com.example.chuong.librarymanagementapi.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;


import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@ActiveProfiles("test")
public class BorrowRepositoryTest {
    @Autowired private TestEntityManager entityManager;
    @Autowired private BorrowRepository borrowRepository;

    private User user;
    private Book book;

    @BeforeEach
    void setUp(){
        user = entityManager.persist(User.builder().username("john").password("x").roles(Set.of(Role.USER)).build());
        book = entityManager.persist(Book.builder().title("DDD").author("Yasuo").price(BigDecimal.valueOf(23948)).totalQuantity(5).availableQuantity(5).build());
    }

    @Test
    void countByUserAndStatus_countsOnlyMatchingStatus(){
        entityManager.persist(Borrow.builder().user(user).book(book).status(Status.BORROWING).borrowDate(LocalDate.now()).returnDate(LocalDate.now().plusDays(4)).build());
        entityManager.persist(Borrow.builder().user(user).book(book).status(Status.RETURNED).borrowDate(LocalDate.now()).returnDate(LocalDate.now().plusDays(4)).build());
        entityManager.flush(); // Đẩy 2 request trên từ bộ nhớ đệm xuống database H2 nhưng chưa commit
        assertThat(borrowRepository.countByUserIdAndStatusIn(user.getId(), Collections.singleton(Status.BORROWING))).isEqualTo(1);
    }

    @Test
    void existsByUserAndBookAndActualReturnDateIsNull_detectsActiveBorrow() {
        entityManager.persist(Borrow.builder()
                .user(user)
                .book(book)
                .status(Status.BORROWING)
                .borrowDate(LocalDate.now())
                .returnDate(LocalDate.now().plusDays(14))
                .actualReturnDate(null)
                .build());
        entityManager.flush();

        assertThat(borrowRepository.existsByUserAndBookAndActualReturnDateIsNull(user, book)).isTrue();
    }

    @Test
    void existsByUserAndBookAndActualReturnDateIsNull_returnsFalseWhenAlreadyReturned() {
        entityManager.persist(Borrow.builder()
                .user(user)
                .book(book)
                .status(Status.RETURNED)
                .borrowDate(LocalDate.now().minusDays(10))
                .returnDate(LocalDate.now().minusDays(3))
                .actualReturnDate(LocalDate.now().minusDays(4))
                .build());
        entityManager.flush();

        assertThat(borrowRepository.existsByUserAndBookAndActualReturnDateIsNull(user, book)).isFalse();
    }

    @Test
    void findAllByStatusAndReturnDateBefore_forOverdueCronJob() {
        Borrow overdueBorrow = entityManager.persist(Borrow.builder()
                .user(user).book(book)
                .status(Status.BORROWING)
                .borrowDate(LocalDate.now().minusDays(20))
                .returnDate(LocalDate.now().minusDays(1))
                .build());
        entityManager.flush();

        List<Borrow> overdue = borrowRepository
                .findAllByStatusAndReturnDateBefore(Status.BORROWING, LocalDate.now());

        assertThat(overdue).hasSize(1);
        assertThat(overdue.get(0).getId()).isEqualTo(overdueBorrow.getId());
    }
}
