package com.example.chuong.librarymanagementapi.controller;

import com.example.chuong.librarymanagementapi.dto.request.BookUpdateRequest;
import com.example.chuong.librarymanagementapi.dto.request.CategoryCreateRequest;
import com.example.chuong.librarymanagementapi.dto.request.CategoryFilterRequest;
import com.example.chuong.librarymanagementapi.dto.request.CategoryUpdateRequest;
import com.example.chuong.librarymanagementapi.dto.request.Page.PageResponse;
import com.example.chuong.librarymanagementapi.dto.response.ApiResponse;
import com.example.chuong.librarymanagementapi.dto.response.BookResponse;
import com.example.chuong.librarymanagementapi.dto.response.CategoryListResponse;
import com.example.chuong.librarymanagementapi.dto.response.CategoryResponse;
import com.example.chuong.librarymanagementapi.entity.Category;
import com.example.chuong.librarymanagementapi.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    /**
     * Danh sách thể loại có phân trang, sắp xếp & lọc theo tên.
     * Ví dụ: GET /api/categories?page=1&size=10&sort=name,asc&name=Khoa học
     */
    @GetMapping("/search")
    public ApiResponse<PageResponse<CategoryResponse>> getCategories(
            @ModelAttribute CategoryFilterRequest filterRequest,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name,asc") String sort
            ){
        PageResponse<CategoryResponse> result = categoryService.searchCategory(
                filterRequest,
                page,
                size,
                sort
        );
        return ApiResponse.<PageResponse<CategoryResponse>> builder()
                .code(200)
                .message("Lấy danh sách thể loại thành công !")
                .result(result)
                .build();
    }
    @PostMapping
    public ResponseEntity<CategoryResponse> insertCategory(
            @Valid @RequestBody CategoryCreateRequest request){
        CategoryResponse categoryResponse = categoryService.createCategory(request);
        return ResponseEntity.ok(categoryResponse);
    }
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@Valid @PathVariable UUID id){
        CategoryResponse categoryResponse = categoryService.getCategoryById(id);
        return ResponseEntity.ok(categoryResponse);
    }
    @GetMapping
    public ResponseEntity<CategoryListResponse> getAllCategories(){
        CategoryListResponse categoryResponseList = categoryService.getAllCategories();
        return ResponseEntity.ok(categoryResponseList);
    }
    @GetMapping("/withBooks")
    public ResponseEntity<List<Category>> getAllCategoriesWithBooks(){
        List<Category> categoryResponseList = categoryService.getAllCategoriesWithBooks();
        return ResponseEntity.ok(categoryResponseList);
    }
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable UUID id
            , @Valid @RequestBody CategoryUpdateRequest request){
        CategoryResponse categoryResponse = categoryService.updateCategory(id,request);
        return ResponseEntity.ok(categoryResponse);
    }

    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable UUID id){
        categoryService.deleteCategory(id);
    }
}
