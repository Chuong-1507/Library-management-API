package com.example.chuong.librarymanagementapi.utils;

import com.example.chuong.librarymanagementapi.config.PaginationUtils;
import com.example.chuong.librarymanagementapi.entity.Enum.ErrorCode;
import com.example.chuong.librarymanagementapi.exception.AppException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.springframework.data.domain.Pageable;
import java.util.List;
/**
 * Unit test cho PaginationUtils — không cần Spring context (@SpringBootTest),
 * vì đây là utility class thuần logic, chạy nhanh, không đụng DB.
 */
public class PaginationUtilsTest {
    private static final List<String> ALLOWED_FIELDS = List.of("id","title","price","createdAt");
    private static final String DEFAULT_FIELD = "createdAt";

    @Nested
    @DisplayName("normalizePage")
    class NormalizePage{
        @Test
        @DisplayName("page < 1 -> ếp về DEFAULT_PAGE (1)")
        void shouldClampPageWhenLessThanOne(){
            assertThat(PaginationUtils.normalizePage(0)).isEqualTo(1);
            assertThat(PaginationUtils.normalizePage(-5)).isEqualTo(1);
        }

        @Test
        @DisplayName("page hợp lệ -> giữ nguyên")
        void shouldKeepValidPage(){
            assertThat(PaginationUtils.normalizePage(3)).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("NormalizeSize")
    class NormalizeSize{
        @Test
        @DisplayName("size <= 0 -> ép về DEFAULT_SIZE (10)")
        void shouldClampSizeWhenNonPositive(){
            assertThat(PaginationUtils.normalizeSize(0)).isEqualTo(10);
            assertThat(PaginationUtils.normalizeSize(-10)).isEqualTo(10);
        }

        @Test
        @DisplayName("size hợp lệ trong khoảng [1,100] -> giữ nguyên")
        void shouldKeepValidSize(){
            assertThat(PaginationUtils.normalizeSize(25)).isEqualTo(25);
        }

        @Test
        @DisplayName("size đúng biên MAX_SIZE (100) -> giữ nguyên, không bị ép ")
        void shouldKeepSizeAtExactMaxBoundary(){
            assertThat(PaginationUtils.normalizeSize(100)).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("createPageable - sort field")
    class SortFieldValidation{
        @Test
        @DisplayName("sort field nằm trong whitelist -> tạo Pageable thành công")
        void shouldCreatePageableWhenSortFieldIsAllowed(){
            Pageable pageable = PaginationUtils.createPageable(
                    1,10,"price,asc",ALLOWED_FIELDS,DEFAULT_FIELD
            );

            assertThat(pageable.getPageNumber()).isZero();// 1-indexed (client) -> 0-indexed (Spring)
            assertThat(pageable.getSort().getOrderFor("price")).isNotNull();
            assertThat(pageable.getSort().getOrderFor("price").getDirection()).isEqualTo(Sort.Direction.ASC);
        }

        @Test
        @DisplayName("sort field không nằm trong whitelist -> ném AppException (INVALID_SORT_FIELD)")
        void shouldThrowWhenSortFieldNotAllowed(){
            assertThatThrownBy(() ->
                    PaginationUtils.createPageable(1, 10, "password,asc", ALLOWED_FIELDS, DEFAULT_FIELD)
            )
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_SORT_FIELD));
        }

        @Test
        @DisplayName("Không truyền sort -> dùng defaultSortField với direction DESC")
        void shouldUseDefaultSortWhenSortIsBlank() {
            Pageable pageable = PaginationUtils.createPageable(
                    1, 10,"", ALLOWED_FIELDS, DEFAULT_FIELD
            );
            assertThat(pageable.getSort().getOrderFor(DEFAULT_FIELD)).isNotNull();
            assertThat(pageable.getSort().getOrderFor(DEFAULT_FIELD).getDirection()).isEqualTo(Sort.Direction.DESC);
        }
        @Test
        @DisplayName("sort = null -> dùng defaultSortField, không NullPointerException")
        void shouldUseDefaultSortWhenSortIsNull() {
            Pageable pageable = PaginationUtils.createPageable(
                    1, 10, null, ALLOWED_FIELDS, DEFAULT_FIELD
            );

            assertThat(pageable.getSort().getOrderFor(DEFAULT_FIELD)).isNotNull();
        }

        @Test
        @DisplayName("sortDIr không hợp lệ -> tự động fall back về 'asc'")
        void shouldFallbackToAscWhenDirectionInvalid(){
            Pageable pageable = PaginationUtils.createPageable(
                    1,10,"price,abc",ALLOWED_FIELDS,DEFAULT_FIELD
            );
            assertThat(pageable.getSort().getOrderFor("price").getDirection()).isEqualTo(Sort.Direction.ASC);
        }

        @Test
        @DisplayName("sortDir viết hoa/thường lẫn lộn -> vẫn parse đúng")
        void shouldHandleCaseInsensitiveDirection(){
            Pageable pageable = PaginationUtils.createPageable(
                    1,10,"price,DESC",ALLOWED_FIELDS,DEFAULT_FIELD
            );
            assertThat(pageable.getSort().getOrderFor("price").getDirection()).isEqualTo(Sort.Direction.DESC);
        }

        @Test
        @DisplayName("chỉ truyền field, không truyền direction (vd: 'price') -> fallback về 'asc'")
        void shouldFallBackToAscWhenDirectionMissing(){
            Pageable pageable = PaginationUtils.createPageable(
                    1,10,"price",ALLOWED_FIELDS,DEFAULT_FIELD
            );
            assertThat(pageable.getSort().getOrderFor("price").getDirection()).isEqualTo(Sort.Direction.ASC);
        }
    }

    @Nested
    @DisplayName("createPageable - 1-indexed to 0-indexed conversion")
    class PageIndexConversion{
        @Test
        @DisplayName("client truyền page = 1 -> Pageable.getPageNumber() = 0")
        void shouldConvertPageOneToZeroIndexed(){
            Pageable pageable = PaginationUtils.createPageable(
                    1,10,"price,asc",ALLOWED_FIELDS,DEFAULT_FIELD
            );
            assertThat(pageable.getPageNumber()).isZero();
        }

        @Test
        @DisplayName("client truyên page = 3 -> Pageable.getPageNumber() = 2")
        void shouldConvertPageThreeToTwoIndexed() {
            Pageable pageable = PaginationUtils.createPageable(
                    3, 10, "", ALLOWED_FIELDS, DEFAULT_FIELD
            );
            assertThat(pageable.getPageNumber()).isEqualTo(2);
        }
    }
}
