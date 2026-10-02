package com.example.chuong.librarymanagementapi.service;

import com.example.chuong.librarymanagementapi.dto.request.CategoryCreateRequest;
import com.example.chuong.librarymanagementapi.dto.request.CategoryUpdateRequest;
import com.example.chuong.librarymanagementapi.dto.response.CategoryResponse;
import com.example.chuong.librarymanagementapi.entity.Category;
import com.example.chuong.librarymanagementapi.entity.Enum.ErrorCode;
import com.example.chuong.librarymanagementapi.exception.AppException;
import com.example.chuong.librarymanagementapi.mapper.CategoryMapper;
import com.example.chuong.librarymanagementapi.repository.CategoryRepository;
import com.example.chuong.librarymanagementapi.service.serviceImpl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;
    @InjectMocks
    private CategoryServiceImpl categoryService;
    @Mock
    private CategoryMapper categoryMapper;

    private Category category;
    @BeforeEach
    void setUp(){
        category = Category.builder()
                .id(UUID.randomUUID())
                .name("Fiction")
                .build();
    }
    @Test
    void createCategory_success() {

        CategoryCreateRequest request = new CategoryCreateRequest("Fiction");
        CategoryResponse response = new CategoryResponse(category.getId(),category.getName());
        when(categoryRepository.existsByNameIgnoreCase("Fiction")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
        when(categoryMapper.createToCategory(request)).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(response);
        CategoryResponse result = categoryService.createCategory(request);

        assertThat(result.getName()).isEqualTo("Fiction");
    }

    @Test
    void createCategory_duplicateName_throwsException() {
        CategoryCreateRequest request = new CategoryCreateRequest("Fiction");
        when(categoryRepository.existsByNameIgnoreCase("Fiction")).thenReturn(true);

        AppException ex = assertThrows(AppException.class,
                () -> categoryService.createCategory(request));

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CATEGORY_EXISTED);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateCategory_notFound_throwsException() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> categoryService.updateCategory(id, new CategoryUpdateRequest("New Name")));

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    void getAllCategories_success() {
        CategoryResponse response = new CategoryResponse(category.getId(), category.getName());
        when(categoryRepository.findAll()).thenReturn(java.util.List.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(response);

        com.example.chuong.librarymanagementapi.dto.response.CategoryListResponse result = categoryService.getAllCategories();

        assertThat(result.getCategories().size()).isEqualTo(1);
        assertThat(result.getCategories().get(0).getName()).isEqualTo("Fiction");
    }
}
