package com.example.chuong.librarymanagementapi.service;

import com.example.chuong.librarymanagementapi.dto.request.CategoryCreateRequest;
import com.example.chuong.librarymanagementapi.dto.request.CategoryFilterRequest;
import com.example.chuong.librarymanagementapi.dto.request.CategoryUpdateRequest;
import com.example.chuong.librarymanagementapi.dto.request.Page.PageResponse;
import com.example.chuong.librarymanagementapi.dto.response.CategoryResponse;
import com.example.chuong.librarymanagementapi.entity.Category;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface CategoryService {
    CategoryResponse createCategory(CategoryCreateRequest request);

    CategoryResponse getCategoryById(UUID id);

    List<CategoryResponse> getAllCategories();

    List<Category> getAllCategoriesWithBooks();

    CategoryResponse updateCategory(
            UUID id,
            CategoryUpdateRequest request
    );

    void deleteCategory(UUID id);

    PageResponse<CategoryResponse> searchCategory(
            CategoryFilterRequest filterRequest,
            int page,
            int size,
            String sort
    );
}
