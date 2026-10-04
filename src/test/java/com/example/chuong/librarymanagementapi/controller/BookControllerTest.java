package com.example.chuong.librarymanagementapi.controller;

import com.example.chuong.librarymanagementapi.dto.request.BookCreateRequest;
import com.example.chuong.librarymanagementapi.dto.request.BookUpdateRequest;
import com.example.chuong.librarymanagementapi.entity.Book;
import com.example.chuong.librarymanagementapi.entity.Category;
import com.example.chuong.librarymanagementapi.entity.Enum.ErrorCode;
import com.example.chuong.librarymanagementapi.repository.BookRepository;
import com.example.chuong.librarymanagementapi.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class BookControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private BookRepository bookRepository;
    @Autowired private CategoryRepository categoryRepository;

    private Category category;

    @BeforeEach
    void setUp() {
        category = categoryRepository.save(Category.builder().name("Tech").build());
    }

    private Book createSampleBook(String title, int totalQuantity, int availableQuantity) {
        return bookRepository.save(Book.builder()
                .title(title)
                .author("Test Author")
                .price(BigDecimal.valueOf(100_000))
                .totalQuantity(totalQuantity)
                .availableQuantity(availableQuantity)
                .category(category)
                .publisher("Publisher")
                .publicationYear(2023)
                .createdAt(LocalDate.now())
                .build());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createBook_success_returns201() throws Exception {
        BookCreateRequest request = new BookCreateRequest();
        request.setTitle("DDD");
        request.setAuthor("Eric Evans");
        request.setPrice(BigDecimal.valueOf(150_000));
        request.setCategoryId(category.getId());
        request.setTotalQuantity(3);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result.totalQuantity").value(3))
                .andExpect(jsonPath("$.result.availableQuantity").value(3));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createBook_asUser_forbidden() throws Exception {
        BookCreateRequest request = new BookCreateRequest();
        request.setTitle("DDD");
        request.setAuthor("Eric Evans");
        request.setPrice(BigDecimal.valueOf(150_000));
        request.setCategoryId(category.getId());
        request.setTotalQuantity(3);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateBook_reduceTotalQuantityBelowBorrowed_returns400() throws Exception {
        Book book = createSampleBook("Refactoring", 10, 4);

        BookUpdateRequest request = BookUpdateRequest.builder()
                .title("Refactoring")
                .author("Martin Fowler")
                .price(BigDecimal.valueOf(200_000))
                .categoryId(category.getId())
                .publisher("Addison-Wesley")
                .publicationYear(1999)
                .totalQuantity(5)
                .build();

        mockMvc.perform(put("/api/books/{id}", book.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_TOTAL_QUANTITY.getCode()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getBooks_authenticated_returns200() throws Exception {
        createSampleBook("Test Book", 1, 1);

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray());
    }

    @Test
    void getBooks_unauthenticated_unauthorized() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getBookById_success_returns200() throws Exception {
        Book book = createSampleBook("Design Patterns", 5, 5);

        mockMvc.perform(get("/api/books/{id}", book.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.title").value("Design Patterns"))
                .andExpect(jsonPath("$.result.author").value("Test Author"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getBookById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/books/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.BOOK_NOT_FOUND.getCode()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchBooks_success_returnsPaginated() throws Exception {
        createSampleBook("Clean Code", 5, 5);

        mockMvc.perform(get("/api/books/search")
                        .param("page", "1")
                        .param("size", "10")
                        .param("title", "Clean"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content").isArray())
                .andExpect(jsonPath("$.result.page").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteBook_success_returns200WithMessage() throws Exception {
        Book book = createSampleBook("To Delete", 1, 1);

        mockMvc.perform(delete("/api/books/{id}", book.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        assertThat(bookRepository.findById(book.getId())).isEmpty();
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteBook_asUser_forbidden() throws Exception {
        Book book = createSampleBook("To Delete", 1, 1);

        mockMvc.perform(delete("/api/books/{id}", book.getId()))
                .andExpect(status().isForbidden());
    }
}
