package com.example.chuong.librarymanagementapi.service;

import com.example.chuong.librarymanagementapi.dto.request.Auth.LoginRequest;
import com.example.chuong.librarymanagementapi.dto.request.Auth.RegisterRequest;
import com.example.chuong.librarymanagementapi.dto.response.Auth.LoginResponse;
import com.example.chuong.librarymanagementapi.entity.Enum.ErrorCode;
import com.example.chuong.librarymanagementapi.entity.Enum.Role;
import com.example.chuong.librarymanagementapi.entity.User;
import com.example.chuong.librarymanagementapi.exception.AppException;
import com.example.chuong.librarymanagementapi.repository.UserRepository;
import com.example.chuong.librarymanagementapi.security.JwtService;
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
    @InjectMocks private AuthServiceImpl authService;

    @Test
    void register_success_passwordIsHashed() {
        RegisterRequest request = new RegisterRequest("newuser", "Password123");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("hashed"); // đảm bảo không lưu plaintext
    }

    @Test
    void register_duplicateUsername_throwsException() {
        RegisterRequest request = new RegisterRequest("existing", "Password123");
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
        User user = User.builder().username("john").password("hashed").build();
        LoginRequest request = new LoginRequest("john", "wrongpass");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "hashed")).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () -> authService.login(request));

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.PASSWORD_FALSE);
    }

    @Test
    void login_success_returnsAccessToken() {
        User user = User.builder().username("john").password("hashed").roles(Set.of(Role.USER)).build();
        LoginRequest request = new LoginRequest("john", "correct");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct", "hashed")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt.token");

        LoginResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("jwt.token");
    }

}
