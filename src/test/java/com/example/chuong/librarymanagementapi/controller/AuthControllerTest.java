package com.example.chuong.librarymanagementapi.controller;

import com.example.chuong.librarymanagementapi.dto.request.Auth.LoginRequest;
import com.example.chuong.librarymanagementapi.dto.request.Auth.RegisterRequest;
import com.example.chuong.librarymanagementapi.entity.Enum.ErrorCode;
import com.example.chuong.librarymanagementapi.entity.Enum.Role;
import com.example.chuong.librarymanagementapi.entity.User;
import com.example.chuong.librarymanagementapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void register_success_returns201() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .password("Password123")
                .email("newuser@example.com")
                .fullName("New User")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        assertThat(userRepository.existsByUsername("newuser")).isTrue();
    }

    @Test
    void register_duplicateUsername_returns400() throws Exception {
        userRepository.save(User.builder().username("existing").password(passwordEncoder.encode("x")).roles(Set.of(Role.USER)).build());

        RegisterRequest request = RegisterRequest.builder()
                .username("existing")
                .password("Password123")
                .email("existing@example.com")
                .fullName("Existing User")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.USERNAME_EXISTED.getCode()));
    }

    @Test
    void login_success_returnsTokenThatActuallyWorks() throws Exception {
        userRepository.save(User.builder().username("john").password(passwordEncoder.encode("correctpass")).roles(Set.of(Role.USER)).build());

        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("john", "correctpass"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(body).path("result").path("accessToken").asString();
        assertThat(token).isNotBlank();

        // dùng đúng token vừa nhận gọi 1 API cần auth khác — verify JWT filter thật chạy đúng
        mockMvc.perform(get("/api/borrows/my-borrows").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
    @Test
    void login_wrongPassword_returns400() throws Exception {
        userRepository.save(User.builder().username("john").password(passwordEncoder.encode("correctpass")).roles(Set.of(Role.USER)).build());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("john", "wrongpass"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.PASSWORD_FALSE.getCode()));
    }

    @Test
    void accessProtectedEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/borrows/my-borrows"))
                .andExpect(status().isUnauthorized());
    }

}
