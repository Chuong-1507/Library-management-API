package com.example.chuong.librarymanagementapi.mapper;

import com.example.chuong.librarymanagementapi.dto.request.CategoryCreateRequest;
import com.example.chuong.librarymanagementapi.dto.request.CategoryUpdateRequest;
import com.example.chuong.librarymanagementapi.dto.response.CategoryResponse;
import com.example.chuong.librarymanagementapi.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "id",ignore = true)
    @Mapping(target = "books",ignore = true)
    Category createToCategory(CategoryCreateRequest request);

    CategoryResponse toResponse(Category category);

    @Mapping(target = "id",ignore = true)
    @Mapping(target = "books",ignore = true)
    void updateCategory(
            CategoryUpdateRequest request,
            @MappingTarget Category category
    );
}
