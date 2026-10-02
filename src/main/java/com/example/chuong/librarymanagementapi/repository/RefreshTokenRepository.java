package com.example.chuong.librarymanagementapi.repository;

import com.example.chuong.librarymanagementapi.entity.RefreshToken;
import com.example.chuong.librarymanagementapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying // Query UPDATE nên cần annotation này
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user = :user AND r.revoked = false")
    void revokeAllByUser(@Param("user")User user); // revoke: thu hồi, hủy quyền (refreshToken)
}
