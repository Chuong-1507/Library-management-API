package com.example.chuong.librarymanagementapi.service;

import com.example.chuong.librarymanagementapi.entity.RefreshToken;
import com.example.chuong.librarymanagementapi.entity.User;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user);

    RefreshToken verifyAndGet(String token);

    void revoke(String token);

    //Vô hiệu hóa toàn bộ refreshToken
    void revokeAllForUser(User user);
}
