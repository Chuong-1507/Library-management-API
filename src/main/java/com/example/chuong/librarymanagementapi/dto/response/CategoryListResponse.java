package com.example.chuong.librarymanagementapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryListResponse {
    private List<CategoryResponse> categories;

    public static CategoryListResponse of(List<CategoryResponse> categories) {
        return CategoryListResponse.builder()
                .categories(categories)
                .build();
    }
}
