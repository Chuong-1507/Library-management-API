package com.example.chuong.librarymanagementapi.service.impl;

import com.example.chuong.librarymanagementapi.dto.request.Auth.LoginRequest;
import com.example.chuong.librarymanagementapi.dto.response.Auth.LoginResponse;
import com.example.chuong.librarymanagementapi.dto.request.Auth.RegisterRequest;
import com.example.chuong.librarymanagementapi.entity.Enum.ErrorCode;
import com.example.chuong.librarymanagementapi.entity.Enum.Role;
import com.example.chuong.librarymanagementapi.entity.User;
import com.example.chuong.librarymanagementapi.exception.AppException;
import com.example.chuong.librarymanagementapi.repository.UserRepository;
import com.example.chuong.librarymanagementapi.security.JwtService;
import com.example.chuong.librarymanagementapi.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.IllegalFormatCodePointException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())){
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(Role.USER));
        userRepository.save(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(()->new AppException(ErrorCode.USER_FALSE));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )){
            throw new AppException(ErrorCode.PASSWORD_FALSE);
        }

        String token = jwtService.generateToken(user);

        return LoginResponse.builder()
                .token(token)
                .build();
    }

}
