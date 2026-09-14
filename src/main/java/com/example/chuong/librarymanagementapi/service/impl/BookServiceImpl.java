package com.example.chuong.librarymanagementapi.service.impl;

import com.example.chuong.librarymanagementapi.config.PaginationUtils;
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
import com.example.chuong.librarymanagementapi.service.BookService;
import com.example.chuong.librarymanagementapi.specification.BookSpecification;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BookMapper mapper;

    private static final List<String> ALLOWED_SORT_FIELDS = List.of(
            "id","title","author","publisher","publicationYear","price","quantity","createdAt"
    );
    private static final String DEFAULT_SORT_FIELD = "createdAt";// Nếu user không truyền tiêu chí sort thì mặc định sắp xếp theo trường createdAt

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public BookResponse createBook(BookCreateRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(()-> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        Book book = mapper.createToBook(request);
        book.setCategory(category);
        book.setCreatedAt(LocalDate.now());

        Book savedBook = bookRepository.save(book);

        return mapper.toResponse(savedBook);
    }

    @Override
    public BookResponse getBookById(UUID id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(()->new AppException(ErrorCode.BOOK_NOT_FOUND));
        return mapper.toResponse(book);
    }

    @Override
    public List<BookResponse> getAllBooks() {
        List<Book> booksList = bookRepository.findAll();
        return booksList.stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<Book> getAllBooksWithCategory() {
        List<Book> booksList = bookRepository.findAllWithCategory();
        return booksList.stream().toList();
    }

    @Override
    public BookResponse updateBook(UUID id, BookUpdateRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(()->new AppException(ErrorCode.BOOK_NOT_FOUND));
        Category updatedCategory = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(()->new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        book.setTitle(request.getTitle());
        book.setPrice(request.getPrice());
        book.setAuthor(request.getAuthor());
        book.setQuantity(request.getQuantity());
        book.setCategory(updatedCategory);
        book.setPublisher(request.getPublisher());
        book.setPublicationYear(request.getPublicationYear());
        book.setCreatedAt(LocalDate.now());

        Book updatedBook = bookRepository.save(book);
        return mapper.toResponse(updatedBook);
    }

    @Override
    public void deleteBook(UUID id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(()->new AppException(ErrorCode.BOOK_NOT_FOUND));
        bookRepository.delete(book);
    }

    //Tìm kiếm theo phân trang
    @Override
    public PageResponse<BookResponse> searchBooks(BookFilterRequest filterRequest, int page, int size, String sort) {
        //1. Validate khoảng giá truớc khi query
        if (filterRequest.isPriceRangeInvalid()){
            throw new AppException(ErrorCode.INVALID_PRICE_RANGE);
        }

        //2. Tạo Pageable an toàn (clamp size, validate whitelist sort field)
        Pageable pageable = PaginationUtils.createPageable(
                page,size,sort,ALLOWED_SORT_FIELDS,DEFAULT_SORT_FIELD
        );

        //3. Build Specification động từ filter (đã kèm fetch join category chống N+1)
        var spec = BookSpecification.withFilter(filterRequest);

        //4. Query
        Page<Book> bookPage = bookRepository.findAll(spec,pageable);

        //5. Map Entity -> DTO bằng MapStruct
        Page<BookResponse> responsePage = bookPage.map(mapper::toResponse);

        //6. Đòng gói PageResponse, trả lại đúng page client đã truyền (1-indexed, đã clamp)
        int normalizedPage = PaginationUtils.normalizePage(page);
        return PageResponse.fromPage(responsePage,normalizedPage);
    }
}
