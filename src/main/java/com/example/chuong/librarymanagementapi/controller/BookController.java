package com.example.chuong.librarymanagementapi.controller;

import com.example.chuong.librarymanagementapi.dto.request.BookCreateRequest;
import com.example.chuong.librarymanagementapi.dto.request.BookFilterRequest;
import com.example.chuong.librarymanagementapi.dto.request.BookUpdateRequest;
import com.example.chuong.librarymanagementapi.dto.request.Page.PageResponse;
import com.example.chuong.librarymanagementapi.dto.response.ApiResponse;
import com.example.chuong.librarymanagementapi.dto.response.BookResponse;

import com.example.chuong.librarymanagementapi.entity.Book;
import com.example.chuong.librarymanagementapi.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;  

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;

    @GetMapping("/search/full-text")
    public ApiResponse<PageResponse<BookResponse>> searchBooks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<BookResponse> result = bookService.searchBooks(keyword, PageRequest.of(page - 1, size));

        return ApiResponse.<PageResponse<BookResponse>>builder()
                .result(PageResponse.fromPage(result))
                .build();
    }

    /**
     * Tìm kiếm & phân trang sách theo nhiều tiêu chí.
     * Ví dụ: GET /api/books/search?page=1&size=10&sort=title,asc&title=Java&minPrice=100000
     */
    @GetMapping("/search")
    public ApiResponse<PageResponse<BookResponse>> searchBooks(
            @ModelAttribute BookFilterRequest filterRequest,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
            ){
        PageResponse<BookResponse> result = bookService.searchBooks(filterRequest,page,size,sort);
        return ApiResponse.<PageResponse<BookResponse>>builder()
                .code(200)
                .message("Tìm kiếm sách thành công")
                .result(result)
                .build();
    }

    @GetMapping
    public ApiResponse<List<BookResponse>> getAllBooks(){
        var result = bookService.getAllBooks();
        return ApiResponse.<List<BookResponse>>builder()
                .code(200)
                .message("Lấy toàn bộ danh sách thành công")
                .result(result)
                .build();

    }
    @GetMapping("/withCategory")
    public ApiResponse<List<Book>> getAllBooksWithCategory(){
        var result = bookService.getAllBooksWithCategory();
        return ApiResponse.<List<Book>>builder()
                .code(200)
                .message("Lấy toàn bộ danh sách cùng Category thành công")
                .result(result)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<BookResponse> getBookById(@PathVariable UUID id){
         var result = bookService.getBookById(id);
         return ApiResponse.<BookResponse>builder()
                 .code(200)
                 .message("Tìm sách thành công")
                 .result(result)
                 .build();
    }

@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public ApiResponse<BookResponse> insertBook(
        @Valid @RequestBody BookCreateRequest request) {

    BookResponse bookResponse = bookService.createBook(request);

    return ApiResponse.<BookResponse>builder()
            .code(200)
            .message("Thêm sách thành công")
            .result(bookResponse)
            .build();
}

    @PutMapping("/{id}")
    public ApiResponse<BookResponse> updateBook(
            @PathVariable UUID id
            ,@Valid @RequestBody BookUpdateRequest request){
        BookResponse result = bookService.updateBook(id,request);
        return ApiResponse.<BookResponse>builder()
                .code(200)
                .message("Lấy toàn bộ danh sách thành công")
                .result(result)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBook(@PathVariable UUID id) {
        bookService.deleteBook(id);
        return ApiResponse.<Void>builder()
                .message("Xóa sách thành công")
                .build();
    }


}
