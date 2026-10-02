package com.example.chuong.librarymanagementapi.service.serviceImpl;

import com.example.chuong.librarymanagementapi.dto.request.Auth.LoginRequest;
import com.example.chuong.librarymanagementapi.dto.request.Auth.RefreshTokenRequest;
import com.example.chuong.librarymanagementapi.dto.response.Auth.LoginResponse;
import com.example.chuong.librarymanagementapi.dto.request.Auth.RegisterRequest;
import com.example.chuong.librarymanagementapi.dto.response.UserResponse;
import com.example.chuong.librarymanagementapi.entity.Enum.ErrorCode;
import com.example.chuong.librarymanagementapi.entity.Enum.Role;
import com.example.chuong.librarymanagementapi.entity.RefreshToken;
import com.example.chuong.librarymanagementapi.entity.User;
import com.example.chuong.librarymanagementapi.exception.AppException;
import com.example.chuong.librarymanagementapi.mapper.UserMapper;
import com.example.chuong.librarymanagementapi.repository.UserRepository;
import com.example.chuong.librarymanagementapi.security.JwtService;
import com.example.chuong.librarymanagementapi.service.AuthService;
import com.example.chuong.librarymanagementapi.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.util.EnumUtils;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .roles(Set.of(Role.USER))
                .build();

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(()->new AppException(ErrorCode.USER_FALSE));

        if (user.getDeletedAt() != null){
            throw new AppException(ErrorCode.USER_FALSE);// coi như không tồn tại, không tiết lộ tài khoản từng có
        }

        if (!user.isEnabled()){
            throw new AppException(ErrorCode.USER_DISABLED);
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )){
            throw new AppException(ErrorCode.PASSWORD_FALSE);
        }

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken= refreshTokenService.createRefreshToken(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    public LoginResponse refresh(RefreshTokenRequest request){
        RefreshToken oldToken = refreshTokenService.verifyAndGet(request.getRefreshToken());
        String newAccessToken = jwtService.generateToken(oldToken.getUser());

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(oldToken.getToken())
                .build();
    }

    public void logout(RefreshTokenRequest request){
        refreshTokenService.revoke(request.getRefreshToken());
    }

}
