package com.example.chuong.librarymanagementapi.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.management.loading.PrivateClassLoader;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Đóng gói các tham số lọc động cho API tìm kiếm sách.
 * Dùng với @ModelAttribute ở Controller để Spring tự bind từ query param.
 * Tất cả field đều optional (nullable) — field nào null thì điều kiện
 * lọc tương ứng sẽ bị bỏ qua trong Specification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookFilterRequest {
    /**
     * Tìm theo tên sách, dạng LIKE %title%, không phân biệt hoa thường.
     */
    private String title;
    /**
     * Tìm theo tác giả, dạng LIKE %author%, không phân biệt hoa thường.
     */
    private String author;
    /**
     * Lọc chính xác theo thể loại.
     */
    private UUID categoryId;
    /**
     * Giá tối thiểu (>=). Phải >= 0 nếu có truyền.
     */
    @DecimalMin(value = "0.0", inclusive = true, message = "minPrice phải >= 0")
    private BigDecimal minPrice;
    /**
     * Giá tối đa (<=). Phải >= 0 nếu có truyền.
     */
    @DecimalMax(value = "0.0", inclusive = true, message = "maxPrice phải >= 0")
    private BigDecimal maxPrice;
    /**
     * Lọc chính xác theo năm xuất bản.
     */
    private Integer pulicationYear;
    /**
     * Kiểm tra nhanh minPrice có lớn hơn maxPrice không (dùng ở Service để validate).
     */
    public boolean isPriceRangeInvalid(){
        return minPrice != null && maxPrice != null & minPrice.compareTo(maxPrice) >0;
    }
}
