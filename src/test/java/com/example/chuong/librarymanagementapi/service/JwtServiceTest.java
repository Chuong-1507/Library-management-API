package com.example.chuong.librarymanagementapi.service;

import com.example.chuong.librarymanagementapi.entity.Enum.Role;
import com.example.chuong.librarymanagementapi.entity.User;
import com.example.chuong.librarymanagementapi.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.BDDAssertions.within;


public class JwtServiceTest {
    private JwtService jwtService;
    private SecretKey secretKey = Keys.hmacShaKeyFor(
            "test-secret-key-minimum-256-bits-xxxxxxxxxxxx".getBytes(StandardCharsets.UTF_8)
    );
    @BeforeEach
    void setUp(){
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtService, "expirationTime", 3_600_000L); // 1h — đổi tên field theo code thật
    }

    @Test
    void generateToken_containCorrectionUsername(){
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("John")
                .roles(Set.of(Role.USER))
                .build();

        String token = jwtService.generateToken(user);
        assertThat(jwtService.extractAllClaims(token).getSubject()).isEqualTo("John");
    }

    @Test
    void generateToken_expiresAfterConfiguredDuration(){
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("John")
                .roles(Set.of(Role.USER))
                .build();

        Instant before = Instant.now();

        String token = jwtService.generateToken(user);
        Date expiration = jwtService.extractAllClaims(token).getExpiration();

        assertThat(expiration.toInstant())
                .isCloseTo(before.plusMillis(3_600_600L),within(5, ChronoUnit.SECONDS));

    }

    @Test
    void isTokenExpired_pastExpiry_returnsTrue(){
        Claims claims = Jwts.claims()
                .expiration(new Date(System.currentTimeMillis() - 10_000L))
                .build();

        assertThat(jwtService.isTokenExpired(claims)).isTrue();
    }
}
