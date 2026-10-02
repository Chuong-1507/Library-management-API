package com.example.chuong.librarymanagementapi.service;

import com.example.chuong.librarymanagementapi.dto.request.Auth.LoginRequest;
import com.example.chuong.librarymanagementapi.dto.request.Auth.RegisterRequest;
import com.example.chuong.librarymanagementapi.dto.response.Auth.LoginResponse;
import com.example.chuong.librarymanagementapi.dto.response.UserResponse;
import com.example.chuong.librarymanagementapi.entity.Enum.ErrorCode;
import com.example.chuong.librarymanagementapi.entity.Enum.Role;
import com.example.chuong.librarymanagementapi.entity.RefreshToken;
import com.example.chuong.librarymanagementapi.entity.User;
import com.example.chuong.librarymanagementapi.exception.AppException;
import com.example.chuong.librarymanagementapi.mapper.UserMapper;
import com.example.chuong.librarymanagementapi.repository.UserRepository;
import com.example.chuong.librarymanagementapi.security.JwtService;
import com.example.chuong.librarymanagementapi.service.RefreshTokenService;
import com.example.chuong.librarymanagementapi.service.serviceImpl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private UserMapper userMapper;
    @Mock private RefreshTokenService refreshTokenService;
    @InjectMocks private AuthServiceImpl authService;

    @Test
    void register_success_passwordIsHashed() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .password("Password123")
                .email("newuser@example.com")
                .fullName("New User")
                .build();
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toUserResponse(any(User.class))).thenReturn(new UserResponse());

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("hashed"); // đảm bảo không lưu plaintext
    }

    @Test
    void register_duplicateUsername_throwsException() {
        RegisterRequest request = RegisterRequest.builder()
                .username("existing")
                .password("Password123")
                .email("existing@example.com")
                .fullName("Existing User")
                .build();
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> authService.register(request));

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.USERNAME_EXISTED);
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_userNotFound_throwsException() {
        LoginRequest request = new LoginRequest("ghost", "whatever");
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> authService.login(request));

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.USER_FALSE);
    }

    @Test
    void login_wrongPassword_throwsException() {
        User user = User.builder().username("john").password("hashed").enabled(true).build();
        LoginRequest request = new LoginRequest("john", "wrongpass");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "hashed")).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () -> authService.login(request));

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.PASSWORD_FALSE);
    }

    @Test
    void login_success_returnsAccessToken() {
        User user = User.builder().username("john").password("hashed").enabled(true).roles(Set.of(Role.USER)).build();
        LoginRequest request = new LoginRequest("john", "correct");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct", "hashed")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt.token");
        RefreshToken refreshToken = RefreshToken.builder().token("refresh.token").build();
        when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);

        LoginResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("jwt.token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh.token");
    }

}
