package com.example.chuong.librarymanagementapi.config;
/*?
* Helper giống như cổng kiểm soát cho pagination
* Nó giúp mọi API list trong prject xử lý phân trang/sắp xếp thông nhất
* */


import com.example.chuong.librarymanagementapi.entity.Enum.ErrorCode;
import com.example.chuong.librarymanagementapi.exception.AppException;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.SplittableRandom;

/**
 * Helper class tạo Pageable một cách an toàn, chuẩn hóa cho toàn bộ các API danh sách.
 * Quy ước áp dụng:
 *  - page (client truyền vào) là 1-indexed; nội bộ Spring Data cần 0-indexed nên sẽ trừ 1.
 * - size bị ép (clamp) về khoảng [1, MAX_SIZE], không ném lỗi khi vượt giới hạn.
 * - sort field phải nằm trong whitelist của từng module, nếu không sẽ ném AppException(INVALID_SORT_FIELD).
 * - sort direction không hợp lệ (khác asc/desc, không phân biệt hoa thường) sẽ tự động fallback về "asc".
 */
@UtilityClass
public class PaginationUtils {
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_SIZE = 10;
    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 100;

    private static final String SORT_PARAM_SEPARATOR = ",";
    private static final String DEFAULT_SORT_DIRECTION = "ASC";

    /**
     * Tạo Pageable đã được validate/clamp đầy đủ.
     *
     * @param page               Số trang client truyền vào (1-indexed). Nếu < 1, sẽ ép về DEFAULT_PAGE.
     * @param size               Kích thước trang client truyền vào. Sẽ bị ép vào khoảng [1, MAX_SIZE].
     * @param sort               Chuỗi sort dạng "fieldName,asc" hoặc "fieldName,desc". Có thể null/rỗng.
     * @param allowedSortFields  Danh sách các field được phép sort của module hiện tại (whitelist).
     * @param defaultSortField   Field mặc định dùng để sort nếu client không truyền "sort".
     * @return Pageable hợp lệ, sẵn sàng dùng cho repository.findAll(...).
     * @throws AppException nếu sort field không thuộc whitelist (ErrorCode.INVALID_SORT_FIELD).
     */
    public static Pageable createPageable(
            int page,
            int size,
            String sort,
            List<String> allowedSortFields,
            String defaultSortField
    ){
        int safePage = normalizePage(page);
        int safeSize = normalizeSize(size);
        Sort sortObj = buildSort(sort,allowedSortFields,defaultSortField);

        // Spring Data JPA dùng 0-indexed nội bộ == page client truyền (1-indexed) - 1
        return PageRequest.of(safePage - 1, safeSize,sortObj);
    }

    /**
     * Ép page về giá trị hợp lệ tối thiểu là 1 (1-indexed).
     */
    public static int normalizePage(int page){
        return page < 1 ? DEFAULT_PAGE : page;
    }
    /**
     * Ép size vào khoảng [MIN_SIZE, MAX_SIZE]. Không ném lỗi, chỉ clamp âm thầm
     * theo đúng quy ước đã chốt (bảo vệ Database khỏi truy vấn quá lớn).
     */
    public static int normalizeSize(int size){
        if (size < MIN_SIZE){
            return DEFAULT_SIZE;
        }
        return Math.min(size,MAX_SIZE);
    }
    /**
     * Parse chuỗi sort (ví dụ "price,desc") thành Sort object, có validate whitelist
     * và fallback an toàn cho cả field lẫn direction.
     */
    private static Sort buildSort(String sort,
                                 List<String> allowedSortFields,
                                 String defaultSortField){
        // Không truyền sort -> dùng default field, direction mặc định (desc để ưu tiên dữ liệu mới nhất)
        if (!StringUtils.hasText(sort)){
            return Sort.by(Sort.Direction.DESC,defaultSortField);
        }
        String[] parts = sort.split(SORT_PARAM_SEPARATOR);
        String fieldName = parts[0].trim();

        //Nếu không truyền fieldName hoặc không tồn tại fieldName trong WhiteList thì ném lỗi
        if (!StringUtils.hasText(fieldName) || !allowedSortFields.contains(fieldName)){
            throw new AppException(ErrorCode.INVALID_SORT_FIELD);
        }

        //Nếu truyền vào đủ (fieldName,Sort) thì truyền Sort vào direction
        Sort.Direction direction = resolveDirection(parts.length > 1 ? parts[1] : null);

        return Sort.by(direction,fieldName);
    }
    /**
     * Xác định hướng sort. Nếu giá trị không hợp lệ (khác asc/desc, không phân biệt hoa thường),
     * tự động fallback về "asc" thay vì ném lỗi.
     */
    private static Sort.Direction resolveDirection(String rawDirection){
        //Nếu không truyền rawDirection -> return về DEFAULT_SORT_DIRECTION
        if (!StringUtils.hasText(rawDirection)){
            return Sort.Direction.fromString(DEFAULT_SORT_DIRECTION);
        }
        //Bắt các trường hợp Direction không hợp lệ
        try {
            return Sort.Direction.fromString(rawDirection.trim());
        }catch (IllegalArgumentException ex){
            return Sort.Direction.fromString(DEFAULT_SORT_DIRECTION);
        }
    }
}
