package com.example.chuong.librarymanagementapi.repository;

import com.example.chuong.librarymanagementapi.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User,UUID> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(@NotBlank @Email String email);

    /**Lấy tất cả các user chưa bị soft delete, có phân trang*/
    Page<User> findAllByDeletedAtIsNull(Pageable pageable);

    Optional<User> findByIdAndDeletedAtIsNull(UUID id);
}
