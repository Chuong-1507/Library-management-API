package com.example.chuong.librarymanagementapi.service;

import com.example.chuong.librarymanagementapi.dto.request.BookCreateRequest;
import com.example.chuong.librarymanagementapi.dto.request.BookFilterRequest;
import com.example.chuong.librarymanagementapi.dto.request.BookUpdateRequest;
import com.example.chuong.librarymanagementapi.dto.request.Page.PageResponse;
import com.example.chuong.librarymanagementapi.dto.response.BookResponse;

import com.example.chuong.librarymanagementapi.entity.Book;

import java.util.List;
import java.util.UUID;

public interface BookService {
    BookResponse createBook(BookCreateRequest request);

    BookResponse getBookById(UUID id);

    List<BookResponse> getAllBooks();

    List<Book> getAllBooksWithCategory();

    BookResponse updateBook(UUID id, BookUpdateRequest request);

    void deleteBook(UUID id);

    //Tìm kiếm theo phân trang
    PageResponse<BookResponse> searchBooks(
            BookFilterRequest filterRequest,
            int page,
            int size,
            String sort
    );
}
