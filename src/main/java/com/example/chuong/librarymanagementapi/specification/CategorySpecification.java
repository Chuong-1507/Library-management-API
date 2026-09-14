package com.example.chuong.librarymanagementapi.specification;

import com.example.chuong.librarymanagementapi.dto.request.CategoryFilterRequest;
import com.example.chuong.librarymanagementapi.entity.Category;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

@UtilityClass
public class CategorySpecification {
    public static Specification<Category> hasName(String name){
        return ((root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(name)){
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),"%" + name.toLowerCase() + "%");
        });
    }
    /**
     * Kết hợp toàn bộ điều kiện từ CategoryFilterRequest.
     * Hiện tại chỉ có 1 field, nhưng viết theo dạng này để dễ mở rộng
     * (ví dụ sau này thêm lọc theo trạng thái active/inactive).
     */
    public static Specification<Category> withFilter(CategoryFilterRequest filter){
        return Specification.where(hasName(filter.getName()));
    }


}
