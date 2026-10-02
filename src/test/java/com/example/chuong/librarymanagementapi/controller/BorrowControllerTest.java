package com.example.chuong.librarymanagementapi.controller;

import com.example.chuong.librarymanagementapi.dto.request.BookCreateRequest;
import com.example.chuong.librarymanagementapi.dto.request.Borrow.BorrowCreateRequest;
import com.example.chuong.librarymanagementapi.dto.response.BorrowResponse;
import com.example.chuong.librarymanagementapi.entity.Book;
import com.example.chuong.librarymanagementapi.entity.Borrow;
import com.example.chuong.librarymanagementapi.entity.Enum.ErrorCode;
import com.example.chuong.librarymanagementapi.entity.Enum.Role;
import com.example.chuong.librarymanagementapi.entity.Enum.Status;
import com.example.chuong.librarymanagementapi.entity.User;
import com.example.chuong.librarymanagementapi.mapper.BorrowMapper;
import com.example.chuong.librarymanagementapi.repository.BookRepository;
import com.example.chuong.librarymanagementapi.repository.BorrowRepository;
import com.example.chuong.librarymanagementapi.repository.UserRepository;
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
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class BorrowControllerTest {
    //MockMvc: giả lập việc gửi HTTP request đến Controller mà không cần chạy server thật
    @Autowired
    private MockMvc mockMvc;
    //ObjectMapper dùng đẻ chuyển đổi giữa Java Object và JSON
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BorrowRepository borrowRepository;


    private Book book;
    private User user;

    @BeforeEach
    void setUp() {
        book = bookRepository.save(Book.builder()
                .title("Clean Architecture")
                .author("Uncle Bob")
                .price(BigDecimal.valueOf(100))
                .totalQuantity(5)
                .availableQuantity(5)
                .build());
        user = userRepository.save(User.builder()
                .username("testuser")
                .password("hashed")
                .roles(Set.of(Role.USER))
                .build());
    }

    /**
     * Tạo sách thành oông -> return 201
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
        //Tạo một user giả đang đăng nhập
    void createBorrow_success_return201() throws Exception {
        BorrowCreateRequest request = new BorrowCreateRequest();
        request.setBookId(book.getId());
        request.setBorrowDate(LocalDate.now());
        request.setReturnDate(LocalDate.now().plusDays(7));

        //Giả lập Client gửi request POST
        mockMvc.perform(post("/api/borrows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result.bookId").value(book.getId().toString()));

        assertThat(bookRepository.findById(book.getId()).orElseThrow().getAvailableQuantity()).isEqualTo(4);
    }

    /**
     * Hết sách để muợn
     * @throws Exception Book_out_of_stock
     */
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void createBorrow_bookOutOfStock_return400() throws Exception {
        BorrowCreateRequest request = new BorrowCreateRequest();
        request.setBookId(book.getId());
        request.setBorrowDate(LocalDate.now());
        request.setReturnDate(LocalDate.now().plusDays(7));

        book.setAvailableQuantity(0);
        bookRepository.save(book);

        mockMvc.perform(post("/api/borrows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.BOOK_OUT_OF_STOCK.getCode()));
    }

    /**
     * Test trả sách thành công -> tăng Số lượng sách có thể mượn
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void returnBook_success_increasesAvailableQuantity() throws Exception {
        book.setAvailableQuantity(4);
        bookRepository.save(book);
        Borrow borrow = borrowRepository.save(Borrow.builder()
                .book(book).user(user).status(Status.BORROWING)
                .borrowDate(LocalDate.now().minusDays(3))
                .returnDate(LocalDate.now().plusDays(11))
                .build());

        mockMvc.perform(put("/api/borrows/{id}/return",borrow.getId()))
                .andExpect(status().isOk());

        assertThat(bookRepository.findById(book.getId()).orElseThrow().getAvailableQuantity()).isEqualTo(5);
    }

    /**
     * Lấy và đếm toàn bộ phiếu mượn của user hiện tại
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getMyBorrows_returnsOnlyOwnBorrows() throws Exception {
        User other = userRepository.save(User.builder().username("other").password("hashed").roles(Set.of(Role.USER)).build());
        borrowRepository.save(Borrow.builder().book(book).user(user).status(Status.BORROWING).borrowDate(LocalDate.now()).returnDate(LocalDate.now().plusDays(7)).build());
        borrowRepository.save(Borrow.builder().book(book).user(other).status(Status.BORROWING).borrowDate(LocalDate.now()).returnDate(LocalDate.now().plusDays(7)).build());

        mockMvc.perform(get("/api/borrows/my-borrows"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content.length()").value(1));
    }

    /**
     * User không đc quyền lấy toàn bộ phiếu mượn (chỉ ADMIN)
     * @throws Exception
     */
    @Test
    @WithMockUser(roles = "USER")
    void getAllBorrows_asUser_forbidden() throws Exception {
        mockMvc.perform(get("/api/borrows"))
                .andExpect(status().isForbidden());
    }

    /**
     * ADMIN được xem toàn bộ borrows
     * @throws Exception
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllBorrows_asAdmin_returnsPaginated() throws Exception {
        borrowRepository.save(Borrow.builder().book(book).user(user).status(Status.BORROWING).borrowDate(LocalDate.now()).returnDate(LocalDate.now().plusDays(7)).build());

        mockMvc.perform(get("/api/borrows").param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.page").value(1));
    }
}
