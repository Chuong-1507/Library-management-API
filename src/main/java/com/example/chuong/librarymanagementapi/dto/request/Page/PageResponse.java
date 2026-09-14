package com.example.chuong.librarymanagementapi.dto.request.Page;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * DTO dùng chung để bọc kết quả phân trang trả về cho client.
 * Quy ước: field "page" luôn là 1-indexed (page = 1 là trang đầu tiên),
 * bất kể Spring Data JPA nội bộ dùng 0-indexed.
 *
 * @param <T> Kiểu dữ liệu của từng phần tử trong danh sách (ví dụ BookResponse, CategoryResponse...)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {
    /**
     * Danh sách dữ liệu của trang hiện tại.
     */
    private List<T> content;
    /**
     * Trang hiện tại, dạng 1-indexed (client truyền page=1 -> trả về page=1).
     */
    private int page;
    /**
     * Kích thước trang (số phần tử tối đa mỗi trang).
     */
    private int size;
    /**
     * Tổng số bản ghi thỏa điều kiện trong CSDL (không chỉ riêng trang hiện tại).
     */
    private long totalElements;
    /**
     * Tổng số trang, tính từ totalElements và size.
     */

    private int totalPages;
    /**
     * true nếu đây là trang đầu tiên.
     */
    private boolean first;
    /**
     * true nếu đây là trang cuối cùng.
     */
    private boolean last;
    /**
     * true nếu trang hiện tại không có phần tử nào (ví dụ page vượt quá totalPages).
     */
    private boolean empty;
    /**
     * Factory method chuyển đổi từ Page&lt;T&gt; (Spring Data, 0-indexed) sang
     * PageResponse&lt;T&gt; (client-facing, 1-indexed).
     *
     * @param pageData Kết quả trả về từ repository (Page&lt;T&gt;), nội bộ 0-indexed.
     * @return PageResponse với "page" đã được quy đổi sang 1-indexed.
     */
    public static <T> PageResponse<T> fromPage(Page<T> pageData){
        return PageResponse.<T>builder()
                .content(pageData.getContent())
                .page(pageData.getNumber() + 1) //0-indexed -> 1-indexed
                .size(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .first(pageData.isFirst())
                .last(pageData.isLast())
                .empty(pageData.isEmpty())
                .build();
    }
    /**
     * Overload tiện dụng khi bạn muốn tự chỉ định số trang trả về (1-indexed)
     * thay vì suy ra từ Page (hữu ích nếu page đã được clamp/validate trước đó
     * và bạn muốn phản hồi đúng giá trị đã validate).
     *
     * @param pageData      Kết quả trả về từ repository (Page&lt;T&gt;).
     * @param requestedPage Số trang client đã yêu cầu, dạng 1-indexed (đã được clamp hợp lệ).
     */
    public static <T> PageResponse<T> fromPage(Page<T> pageData, int requestedPage) {
        return PageResponse.<T>builder()
                .content(pageData.getContent())
                .page(requestedPage)
                .size(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .first(pageData.isFirst())
                .last(pageData.isLast())
                .empty(pageData.isEmpty())
                .build();

    }
}
