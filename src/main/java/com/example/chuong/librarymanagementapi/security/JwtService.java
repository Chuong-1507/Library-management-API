package com.example.chuong.librarymanagementapi.security;

import com.example.chuong.librarymanagementapi.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long expirationTime;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // Sinh Access Token từ User
    public String generateToken(User user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> "ROLE_" + role.name())
                .toList();

        return Jwts.builder()
                .id(UUID.randomUUID().toString()) // jti (JWT ID cho Redis blacklist sau này)
                .subject(user.getUsername())
                .claim("type", "access") // Phân biệt Access Token
                .claim("roles", roles)
                .claim("userId", user.getId().toString())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey)
                .compact();
    }

    // Parse & Verify Chữ ký 1 lần duy nhất (Ném ngoại lệ cụ thể nếu Token lỗi)
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(Claims claims) {
        return claims.getSubject();
    }

    // Trích xuất Type (access / refresh)
    public String extractTokenType(Claims claims) {
        return claims.get("type", String.class);
    }

    // Trích xuất Roles an toàn (Type-safe)
    public List<String> extractRoles(Claims claims) {
        Object rolesObject = claims.get("roles");
        if (rolesObject instanceof List<?> list) {
            return list.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }
        return List.of();
    }

    public Date extractExpiration(String token){
        return extractAllClaims(token).getExpiration();
    }

    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}
