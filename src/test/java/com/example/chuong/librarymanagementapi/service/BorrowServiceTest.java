package com.example.chuong.librarymanagementapi.service;

import com.example.chuong.librarymanagementapi.dto.request.Borrow.BorrowCreateRequest;
import com.example.chuong.librarymanagementapi.dto.response.BorrowResponse;
import com.example.chuong.librarymanagementapi.entity.Book;
import com.example.chuong.librarymanagementapi.entity.Borrow;
import com.example.chuong.librarymanagementapi.entity.Enum.ErrorCode;
import com.example.chuong.librarymanagementapi.entity.Enum.Status;
import com.example.chuong.librarymanagementapi.entity.User;
import com.example.chuong.librarymanagementapi.exception.AppException;
import com.example.chuong.librarymanagementapi.mapper.BorrowMapper;
import com.example.chuong.librarymanagementapi.repository.BookRepository;
import com.example.chuong.librarymanagementapi.repository.BorrowRepository;
import com.example.chuong.librarymanagementapi.repository.UserRepository;
import com.example.chuong.librarymanagementapi.service.EmailService;
import com.example.chuong.librarymanagementapi.service.serviceImpl.BorrowServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Mockito test riêng, kiểm tra logic class BorrowServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {

    //Tạo Dependency giả, mô phỏng các method trong nó nhưng không hiểu rõ logic thật
    //Ta phải tự cấu hình (when) cho các method
    @Mock
    private BorrowRepository borrowRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BorrowMapper borrowMapper;

    @Mock
    private EmailService emailService;

    //Tạo BorrowServiceImpl thật, sau đó inject các Mock Dependency vào nó
    @InjectMocks
    private BorrowServiceImpl borrowService;

    private User testUser;
    private Book testBook;
    private Borrow testBorrow;
    private UUID userId;
    private UUID bookId;
    private UUID borrowId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        bookId = UUID.randomUUID();
        borrowId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setUsername("testuser");

        testBook = new Book();
        testBook.setId(bookId);
        testBook.setTitle("Clean Code");
        testBook.setTotalQuantity(10);
        testBook.setAvailableQuantity(5);

        testBorrow = new Borrow();
        testBorrow.setId(borrowId);
        testBorrow.setUser(testUser);
        testBorrow.setBook(testBook);
        testBorrow.setBorrowDate(LocalDate.now().minusDays(10));
        testBorrow.setReturnDate(LocalDate.now().minusDays(3)); // Quá hạn 3 ngày
        testBorrow.setStatus(Status.BORROWING);
    }

    @Test
    void createBorrow_Success() {
        BorrowCreateRequest request = new BorrowCreateRequest();
        request.setBookId(bookId);
        request.setBorrowDate(LocalDate.now());
        request.setReturnDate(LocalDate.now().plusDays(7));

        BorrowResponse mockResponse = new BorrowResponse();
        mockResponse.setId(borrowId);
        mockResponse.setStatus(Status.BORROWING);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(borrowRepository.countByUserIdAndStatusIn(eq(userId), any())).thenReturn(2L);
        when(borrowRepository.existsByUserIdAndBookIdAndStatusIn(eq(userId), eq(bookId), any())).thenReturn(false);
        when(bookRepository.findByIdWithLock(bookId)).thenReturn(Optional.of(testBook));
        when(borrowRepository.save(any(Borrow.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(borrowMapper.toResponse(any(Borrow.class))).thenReturn(mockResponse);

        BorrowResponse result = borrowService.createBorrow("testuser", request);

        assertNotNull(result);
        assertEquals(4, testBook.getAvailableQuantity()); // Đã trừ tồn kho từ 5 -> 4
        verify(bookRepository).findByIdWithLock(bookId);
        verify(borrowRepository).save(any(Borrow.class));
    }

    //out of stock
    @Test
    void createBorrow_OutOfStock_ThrowsExceptions(){

        BorrowCreateRequest request = new BorrowCreateRequest();
        request.setBookId(bookId);
        request.setBorrowDate(LocalDate.now());
        request.setReturnDate(LocalDate.now().plusDays(7));

        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
        when(bookRepository.findByIdWithLock(request.getBookId())).thenReturn(Optional.of(testBook));
        testBook.setAvailableQuantity(0);
        assertThatThrownBy(() -> borrowService.createBorrow(testUser.getUsername(), request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOK_OUT_OF_STOCK);
    }
    //trùng sách đã mượn
    @Test
    void createBorrow_DuplicateBorrow_ThrowsException() {
        BorrowCreateRequest request = new BorrowCreateRequest();
        request.setBookId(bookId);
        request.setBorrowDate(LocalDate.now());
        request.setReturnDate(LocalDate.now().plusDays(7));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(borrowRepository.countByUserIdAndStatusIn(eq(userId), any())).thenReturn(1L);
        when(borrowRepository.existsByUserIdAndBookIdAndStatusIn(eq(userId), eq(bookId), any())).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> borrowService.createBorrow("testuser", request));
        assertEquals(ErrorCode.ALREADY_BORROWED, ex.getErrorCode());
    }

    //borrow exceed limit 5
    @Test
    void createBorrow_ExceedLimit5_ThrowsException(){
        BorrowCreateRequest request = new BorrowCreateRequest();
        request.setBookId(bookId);
        request.setBorrowDate(LocalDate.now());
        request.setReturnDate(LocalDate.now().plusDays(7));

        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
        when(borrowRepository.countByUserIdAndStatusIn(testUser.getId(), List.of(Status.BORROWING,Status.OVERDUE))).thenReturn(5L);

        assertThatThrownBy(() -> borrowService.createBorrow(testUser.getUsername(),request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode",ErrorCode.BORROW_LIMIT_EXCEEDED);

    }
    //trả lại sách sau khi tính tiền phạt và khóa sách
    @Test
    void returnBook_LateReturn_CalculatesFineAndLocksBook() {
        BorrowResponse mockResponse = new BorrowResponse();
        mockResponse.setId(borrowId);

        when(borrowRepository.findById(borrowId)).thenReturn(Optional.of(testBorrow));
        when(bookRepository.findByIdWithLock(bookId)).thenReturn(Optional.of(testBook));
        when(borrowRepository.save(any(Borrow.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(borrowMapper.toResponse(any(Borrow.class))).thenReturn(mockResponse);

        BorrowResponse result = borrowService.returnBook(borrowId, "testuser", false);

        assertNotNull(result);
        assertEquals(Status.RETURNED, testBorrow.getStatus());
        assertEquals(LocalDate.now(), testBorrow.getActualReturnDate());
        assertEquals(0, BigDecimal.valueOf(15000).compareTo(testBorrow.getFineAmount()));// 3 ngày quá hạn * 5000 = 15000
        assertEquals(6, testBook.getAvailableQuantity()); // Đã cộng trả tồn kho từ 5 -> 6
        verify(bookRepository).findByIdWithLock(bookId);
    }

    @Test
    void updateOverdueBorrows_CallsRepository() {
        when(borrowRepository.updateOverdueStatus(eq(Status.BORROWING), eq(Status.OVERDUE), any(LocalDate.class)))
                .thenReturn(5);

        int count = borrowService.updateOverdueBorrows();

        assertEquals(5, count);
        verify(borrowRepository).updateOverdueStatus(eq(Status.BORROWING), eq(Status.OVERDUE), any(LocalDate.class));
    }
}
