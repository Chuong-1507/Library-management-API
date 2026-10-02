package com.example.chuong.librarymanagementapi.service.serviceImpl;

import com.example.chuong.librarymanagementapi.dto.request.Auth.ChangePasswordRequest;
import com.example.chuong.librarymanagementapi.dto.request.Auth.UserAdminUpdateRequest;
import com.example.chuong.librarymanagementapi.dto.request.Auth.UserSelfUpdateRequest;
import com.example.chuong.librarymanagementapi.dto.response.UserResponse;
import com.example.chuong.librarymanagementapi.entity.Enum.ErrorCode;
import com.example.chuong.librarymanagementapi.entity.User;
import com.example.chuong.librarymanagementapi.exception.AppException;
import com.example.chuong.librarymanagementapi.mapper.UserMapper;
import com.example.chuong.librarymanagementapi.repository.UserRepository;
import com.example.chuong.librarymanagementapi.service.RefreshTokenService;
import com.example.chuong.librarymanagementapi.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

//    ---ADMIN----
    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAllByDeletedAtIsNull(pageable).map(userMapper::toUserResponse);
    }


    @Override
    public UserResponse getUserById(UUID id) {
        return userMapper.toUserResponse(findActiveUserOrThrow(id));
    }

    @Override
    @Transactional
    public UserResponse updateUserByAdmin(UUID id, UserAdminUpdateRequest request) {
        User user = findActiveUserOrThrow(id);

        if (request.getRoles() != null){
            user.setRoles(request.getRoles());
        }
        if (request.getEnabled() != null){
            user.setEnabled(request.getEnabled());
            if (!request.getEnabled()){
                refreshTokenService.revokeAllForUser(user);//khóa tài khoản → hủy luôn phiên đang đăng nhập
            }
        }
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void softDeleteUser(UUID id) {
        User user = findActiveUserOrThrow(id);
        user.setDeletedAt(LocalDateTime.now());
        user.setEnabled(false);
        userRepository.save(user);
        refreshTokenService.revokeAllForUser(user);// thu hồi toàn bộ refresh token sau khi xóa mềm user
    }

//    ----SELF-SERVICE----
    @Override
    public UserResponse getMe(String username) {
        return userMapper.toUserResponse(findByUsernameOrThrow(username));
    }

    @Override
    @Transactional
    public UserResponse updateMe(String username, UserSelfUpdateRequest request) {
        User user = findByUsernameOrThrow(username);
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getFullname() != null) user.setFullName(request.getFullname());
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = findByUsernameOrThrow(username);

        if (!passwordEncoder.matches(request.getCurrentPassword(),user.getPassword())){
            throw new AppException(ErrorCode.PASSWORD_FALSE);
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenService.revokeAllForUser(user);
    }

    private User findActiveUserOrThrow(UUID id){
        return userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private User findByUsernameOrThrow(String username){
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
