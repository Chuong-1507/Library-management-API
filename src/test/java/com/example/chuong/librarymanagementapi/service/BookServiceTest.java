package com.example.chuong.librarymanagementapi.service;

import com.example.chuong.librarymanagementapi.dto.request.BookCreateRequest;
import com.example.chuong.librarymanagementapi.dto.request.BookFilterRequest;
import com.example.chuong.librarymanagementapi.dto.request.BookUpdateRequest;
import com.example.chuong.librarymanagementapi.dto.request.Page.PageResponse;
import com.example.chuong.librarymanagementapi.dto.response.BookResponse;
import com.example.chuong.librarymanagementapi.entity.Book;
import com.example.chuong.librarymanagementapi.entity.Category;
import com.example.chuong.librarymanagementapi.entity.Enum.ErrorCode;
import com.example.chuong.librarymanagementapi.exception.AppException;
import com.example.chuong.librarymanagementapi.mapper.BookMapper;
import com.example.chuong.librarymanagementapi.repository.BookRepository;
import com.example.chuong.librarymanagementapi.repository.CategoryRepository;
import com.example.chuong.librarymanagementapi.service.serviceImpl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {
    @Mock
    private BookRepository bookRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private BookMapper mapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book book;
    private Category category;
    private BookResponse bookResponse;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(UUID.randomUUID())
                .name("animation")
                .build();

        book = Book.builder()
                .id(UUID.randomUUID())
                .title("One Piece")
                .author("Ẩn danh")
                .price(BigDecimal.valueOf(12300000))
                .publicationYear(2000)
                .totalQuantity(10)
                .availableQuantity(4)
                .category(category)
                .build();

        bookResponse = BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .price(book.getPrice())
                .totalQuantity(book.getTotalQuantity())
                .availableQuantity(book.getAvailableQuantity())
                .categoryName(category.getName())
                .build();
    }

    @Test
    void createBook_Success() {
        BookCreateRequest request = new BookCreateRequest();
        request.setTitle(book.getTitle());
        request.setAuthor(book.getAuthor());
        request.setCategoryId(book.getCategory().getId());
        request.setPrice(book.getPrice());
        request.setTotalQuantity(book.getTotalQuantity());

        Book bookToSave = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .price(request.getPrice())
                .totalQuantity(request.getTotalQuantity())
                .build();

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(mapper.createToBook(any(BookCreateRequest.class))).thenReturn(bookToSave);
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Book.class))).thenReturn(bookResponse);

        BookResponse result = bookService.createBook(request);

        assertThat(result).isNotNull();
        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(captor.capture());
        assertThat(captor.getValue().getTotalQuantity()).isEqualTo(10);
        assertThat(captor.getValue().getAvailableQuantity()).isEqualTo(10); // mới tạo -> available = total
    }

    @Test
    void createBook_categoryNotFound_throwsException() {
        BookCreateRequest request = new BookCreateRequest();
        request.setCategoryId(UUID.randomUUID());

        when(categoryRepository.findById(any())).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> bookService.createBook(request));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void getBookById_success() {
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(mapper.toResponse(book)).thenReturn(bookResponse);

        BookResponse response = bookService.getBookById(book.getId());

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(book.getId());
    }

    @Test
    void getBookById_notFound_throwsException() {
        UUID id = UUID.randomUUID();
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> bookService.getBookById(id));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BOOK_NOT_FOUND);
    }

    @Test
    void getAllBooks_success() {
        when(bookRepository.findAll()).thenReturn(List.of(book));
        when(mapper.toResponse(book)).thenReturn(bookResponse);

        List<BookResponse> result = bookService.getAllBooks();

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void getAllBooksWithCategory_success() {
        when(bookRepository.findAllWithCategory()).thenReturn(List.of(book));

        List<Book> result = bookService.getAllBooksWithCategory();

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getCategory()).isEqualTo(category);
    }

    @Test
    void updateBook_success() {
        BookUpdateRequest request = new BookUpdateRequest();
        request.setTitle("Updated Title");
        request.setAuthor("Updated Author");
        request.setCategoryId(category.getId());
        request.setPrice(BigDecimal.valueOf(150000));

        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Book.class))).thenReturn(bookResponse);

        BookResponse result = bookService.updateBook(book.getId(), request);

        assertThat(result).isNotNull();
        verify(bookRepository).save(book);
        assertThat(book.getTitle()).isEqualTo("Updated Title");
        assertThat(book.getAuthor()).isEqualTo("Updated Author");
    }

    @Test
    void updateBook_bookNotFound_throwsException() {
        UUID id = UUID.randomUUID();
        BookUpdateRequest request = new BookUpdateRequest();

        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> bookService.updateBook(id, request));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BOOK_NOT_FOUND);
    }

    @Test
    void updateBook_categoryNotFound_throwsException() {
        BookUpdateRequest request = new BookUpdateRequest();
        request.setCategoryId(UUID.randomUUID());

        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(categoryRepository.findById(request.getCategoryId())).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> bookService.updateBook(book.getId(), request));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    void updateBook_reduceTotalQuantityBelowBorrowed_throwsException() {
        // book hiện tại: total=10, available=4 -> đang mượn 6
        BookUpdateRequest request = new BookUpdateRequest();
        request.setTotalQuantity(3); // < 6 -> ko đủ tổng số sách để mượn 6
        request.setTitle(book.getTitle());
        request.setAuthor(book.getAuthor());
        request.setCategoryId(book.getCategory().getId());
        request.setPrice(book.getPrice());

        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));

        AppException exception = assertThrows(AppException.class,
                () -> bookService.updateBook(book.getId(), request));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOTAL_QUANTITY);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void updateBook_increaseTotalQuantity_availableIncreasesBySameDelta() {
        BookUpdateRequest request = new BookUpdateRequest();
        request.setTotalQuantity(15);
        request.setTitle(book.getTitle());
        request.setAuthor(book.getAuthor());
        request.setCategoryId(book.getCategory().getId());
        request.setPrice(book.getPrice());

        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        bookService.updateBook(book.getId(), request);
        assertThat(book.getTotalQuantity()).isEqualTo(15);
        assertThat(book.getAvailableQuantity()).isEqualTo(9);
    }

    @Test
    void deleteBook_success() {
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        bookService.deleteBook(book.getId());
        verify(bookRepository).delete(book);
    }

    @Test
    void deleteBook_notFound_throwsException() {
        UUID id = UUID.randomUUID();
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> bookService.deleteBook(id));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BOOK_NOT_FOUND);
        verify(bookRepository, never()).delete(any(Book.class));
    }

    @Test
    void searchBooks_invalidPriceRange_throwsException() {
        BookFilterRequest filterRequest = BookFilterRequest.builder()
                .minPrice(BigDecimal.valueOf(200))
                .maxPrice(BigDecimal.valueOf(100))
                .build();

        AppException exception = assertThrows(AppException.class,
                () -> bookService.searchBooks(filterRequest, 1, 10, null));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_PRICE_RANGE);
    }

    @Test
    void searchBooks_emptyPage_returnsEmptyWithoutError() {
        BookFilterRequest filterRequest = new BookFilterRequest();
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Book> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(bookRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

        PageResponse<BookResponse> response = bookService.searchBooks(filterRequest, 1, 10, null);

        assertThat(response).isNotNull();
        assertThat(response.getContent().isEmpty()).isTrue();
        assertThat(response.getTotalElements()).isEqualTo(0);
    }

    @Test
    void searchBooks_success_returnsPageResponse() {
        BookFilterRequest filterRequest = new BookFilterRequest();
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Book> bookPage = new PageImpl<>(List.of(book), pageable, 1);

        when(bookRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(bookPage);
        when(mapper.toResponse(book)).thenReturn(bookResponse);

        PageResponse<BookResponse> response = bookService.searchBooks(filterRequest, 1, 10, null);

        assertThat(response).isNotNull();
        assertThat(response.getContent().size()).isEqualTo(1);
        assertThat(response.getContent().get(0).getTitle()).isEqualTo("One Piece");
    }
}
