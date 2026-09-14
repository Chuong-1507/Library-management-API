package com.example.chuong.librarymanagementapi.service.impl;

import com.example.chuong.librarymanagementapi.config.PaginationUtils;
import com.example.chuong.librarymanagementapi.dto.request.CategoryCreateRequest;
import com.example.chuong.librarymanagementapi.dto.request.CategoryFilterRequest;
import com.example.chuong.librarymanagementapi.dto.request.CategoryUpdateRequest;
import com.example.chuong.librarymanagementapi.dto.request.Page.PageResponse;
import com.example.chuong.librarymanagementapi.dto.response.CategoryResponse;
import com.example.chuong.librarymanagementapi.entity.Category;
import com.example.chuong.librarymanagementapi.entity.Enum.ErrorCode;
import com.example.chuong.librarymanagementapi.exception.AppException;
import com.example.chuong.librarymanagementapi.mapper.CategoryMapper;
import com.example.chuong.librarymanagementapi.repository.CategoryRepository;
import com.example.chuong.librarymanagementapi.service.CategoryService;
import com.example.chuong.librarymanagementapi.specification.CategorySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    private static final List<String> ALLOWED_SORT_FIELDS = List.of("id","name");
    private static final String DEFAULT_SORT_FIELD = "name";

    @Override
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())){
            throw new AppException(ErrorCode.CATEGORY_EXISTED);
        }
        Category category = categoryMapper.createToCategory(request);
        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    public CategoryResponse getCategoryById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        return categoryMapper.toResponse(category);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        List<Category> categoryList = categoryRepository.findAll();
        return categoryList.stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    public List<Category> getAllCategoriesWithBooks() {
        List<Category> categoryList = categoryRepository.findAllWithBooks();
        return categoryList.stream()
                .toList();
    }

    @Override
    public CategoryResponse updateCategory(UUID id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        category.setName(request.getName());
        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toResponse(updatedCategory);
    }

    @Override
    public void deleteCategory(UUID id) {
        Category category = categoryRepository.findById(id)
                        .orElseThrow(()->new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        categoryRepository.delete(category);

    }

    @Override
    public PageResponse<CategoryResponse> searchCategory(CategoryFilterRequest filterRequest, int page, int size, String sort) {
        //1. Tạo Pageable an toàn (clamp sizem, validate whitelist sort field)
        Pageable pageable = PaginationUtils.createPageable(
                page,size,sort,ALLOWED_SORT_FIELDS,DEFAULT_SORT_FIELD
        );

        //2. Build Specification động từ filter
        var spec = CategorySpecification.withFilter(filterRequest);

        //3. Query
        Page<Category> categories = categoryRepository.findAll(spec,pageable);

        //4. Map Entity -> DTO bằng MapStruct
        Page<CategoryResponse> responsePage = categories.map(categoryMapper::toResponse);

        //5. Đóng gói PageResponse, trả lại đúng page client đã truyền (1-indexed, đã clamp)
        int normalizePage = PaginationUtils.normalizePage(page);
        return PageResponse.fromPage(responsePage,normalizePage);
    }
}
