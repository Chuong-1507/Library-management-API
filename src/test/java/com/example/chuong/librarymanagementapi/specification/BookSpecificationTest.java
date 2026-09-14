package com.example.chuong.librarymanagementapi.specification;

import com.example.chuong.librarymanagementapi.config.PaginationUtils;
import com.example.chuong.librarymanagementapi.dto.request.BookFilterRequest;
import com.example.chuong.librarymanagementapi.entity.Book;
import com.example.chuong.librarymanagementapi.entity.Category;
import com.example.chuong.librarymanagementapi.repository.BookRepository;
import com.example.chuong.librarymanagementapi.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.beanvalidation.SpringConstraintValidatorFactory;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <p>
 * Integration test cho BookSpecification, dùng @DataJpaTest với H2 in-memory DB
 * (Spring Boot tự cấu hình datasource test khi có dependency com.h2database:h2
 * trong build.gradle, scope testImplementation).
 *</p>
 * <p>
 * Mỗi test tự tạo dữ liệu riêng trong @BeforeEach để độc lập, không phụ thuộc
 * thứ tự chạy test.
 *</p>
 * <p>
 * DataJpaTest: Test Repository/JPA có thực sự làm việc đúng với database không.
 *</p>
 */

@DataJpaTest
public class BookSpecificationTest {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category fictionCategory;
    private Category scienceCategory;

    @BeforeEach
    void setUp(){
        bookRepository.deleteAll();

        fictionCategory = Category.builder().name("fiction").build();
        scienceCategory = Category.builder().name("science").build();

        categoryRepository.save(fictionCategory);
        categoryRepository.save(scienceCategory);

        saveBook("Clean Code","Robert Martin", scienceCategory,new BigDecimal("250000"),2008);
        saveBook("Clean Architecture", "Robert Martin", scienceCategory, new BigDecimal("300000"), 2017);
        saveBook("The Hobbit", "J.R.R. Tolkien", fictionCategory, new BigDecimal("150000"), 1937);
        saveBook("Dune", "Frank Herbert", fictionCategory, new BigDecimal("180000"), 1965);
    }

    private void saveBook(String title, String author, Category category, BigDecimal price, int year){
        Book book = Book.builder()
                .title(title)
                .author(author)
                .category(category)
                .price(price)
                .publicationYear(year)
                .quantity(5)
                .build();
        bookRepository.save(book);
    }

    @Test
    @DisplayName("hasTitle: tìm 'clean' (Không phân biệt hoa thường) -> trả về 2 sách")
    void shouldFilterByTitleCaseInsensitive(){
        var spec = BookSpecification.hasTitle("clean");

        Page<Book> result = bookRepository.findAll(spec,defaultPageable());

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Book::getTitle)
                .containsExactlyInAnyOrder("Clean Code", "Clean Architecture");

    }

    @Test
    @DisplayName("hasAuthor: tìm 'martin' -> trả về đúng 2 sách của Robert Martin")
    void shouldFilterByAuthorCaseInsensitive() {
        var spec = BookSpecification.hasAuthor("martin");

        Page<Book> result =
                bookRepository.findAll(spec, defaultPageable());

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("hasCategory: lọc theo CategoryId -> chỉ trả về sách đúng thể loại")
    void shouldFilterByCategory(){
        var spec = BookSpecification.hasCategory(scienceCategory.getId());
        Page<Book> result = bookRepository.findAll(spec,defaultPageable());

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("hasPriceBetween: minPrice = 160000, maxPrice = 260000 -> trả về sách trong khoảng giá")
    void shouldFilterByPriceRange(){
        var spec = BookSpecification.hasPriceBetween(
                new BigDecimal(160000),new BigDecimal(260000)
        );
        Page<Book> result = bookRepository.findAll(spec,defaultPageable());

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Book::getTitle)
                .containsExactlyInAnyOrder("Clean Code", "Dune");

    }

    @Test
    @DisplayName("hasPriceBetween: chỉ truyền minPrice -> trả về sách có giá >= minPrice")
    void shouldFilterByMinPriceOnly(){
        var spec = BookSpecification.hasPriceBetween(
                new BigDecimal("200000"),null
        );
        Page<Book> result = bookRepository.findAll(spec,defaultPageable());

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("withFilter: kết hợp title + category cùng lúc -> AND đúng, không phải OR")
    void shouldCombineMultipleFiltersWithAnd(){
        BookFilterRequest filterRequest = BookFilterRequest.builder()
                .title("clean")
                .categoryId(fictionCategory.getId())
                .build();

        var spec = BookSpecification.withFilter(filterRequest);
        Page<Book> result = bookRepository.findAll(spec,defaultPageable());

        // Kết hợp AND: title chứa "clean" NHƯNG lại lọc category=Fiction -> không match ai cả
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("withFilter: filter rỗng toàn bộ -> trả về tất cả sách, không lỗi")
    void shouldReturnAllBookWhenFilterIsEmpty(){
        BookFilterRequest emptyFilter = BookFilterRequest.builder().build();

        var spec = BookSpecification.withFilter(emptyFilter);
        Page<Book> result = bookRepository.findAll(spec,defaultPageable());

        assertThat(result.getTotalElements()).isEqualTo(4);
    }

    @Test
    @DisplayName("hasPublicationYear: lọc đúng năm xuất bản")
    void shouldFilterByPublicationYear(){
        var spec = BookSpecification.hasPublicationYear(2008);
        Page<Book> result = bookRepository.findAll(spec,defaultPageable());

        assertThat(result.getContent()).hasSize(1);
    }

    private Pageable defaultPageable(){
        return PageRequest.of(0,10);
    }
}
