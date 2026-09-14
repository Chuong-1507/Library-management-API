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

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;  

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;

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
    public ResponseEntity<List<BookResponse>> getAllBooks(){
        List<BookResponse> bookResponseList = bookService.getAllBooks();
        return ResponseEntity.ok(bookResponseList);
    }
    @GetMapping("/withCategory")
    public ResponseEntity<List<Book>> getAllBooksWithCategory(){
        List<Book> bookList = bookService.getAllBooksWithCategory();
        return ResponseEntity.ok(bookList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable UUID id){
         BookResponse bookResponse = bookService.getBookById(id);
         return ResponseEntity.ok(bookResponse);
    }

@PostMapping
public ResponseEntity<ApiResponse<BookResponse>> insertBook(
        @Valid @RequestBody BookCreateRequest request) {

    BookResponse bookResponse = bookService.createBook(request);

    ApiResponse<BookResponse> response = ApiResponse.<BookResponse>builder()
            .code(200)
            .message("Thêm sách thành công")
            .result(bookResponse)
            .build();

    return ResponseEntity.ok(response);
}

    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable UUID id
            ,@Valid @RequestBody BookUpdateRequest request){
        BookResponse bookResponse = bookService.updateBook(id,request);
        return ResponseEntity.ok(bookResponse);
    }

    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable UUID id){
        bookService.deleteBook(id);
    }


}
