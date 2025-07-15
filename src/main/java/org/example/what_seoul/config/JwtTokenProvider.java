package org.example.what_seoul.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.security.Key;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {
    @Value("${jwt.access-secret}")
    private String SECRET_KEY;

    @Value("${jwt.access-expiration}")
    private Long ACCESS_EXPIRATION_MS;

    @Value("${jwt.refresh-expiration}")
    private Long REFRESH_EXPIRATION_MS;

    private Key getSecretKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public Long getAccessTokenExpirationMs() {
        return ACCESS_EXPIRATION_MS;
    }

    public Long getRefreshTokenExpirationMs() {
        return REFRESH_EXPIRATION_MS;
    }

    public long getAccessTokenExpirationTime(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration().getTime();
    }


    public String generateAccessToken(String adminId, String role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + ACCESS_EXPIRATION_MS);

        return Jwts.builder()
                .setSubject(adminId)
                .claim("type", "access")
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();

    }

    public String generateRefreshToken(String adminId, String role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + REFRESH_EXPIRATION_MS);

        return Jwts.builder()
                .setSubject(adminId)
                .claim("type", "refresh")
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();

    }

    public boolean validateToken(String token) {

        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            log.warn("JWT 검증 실패: {}", exception.getMessage());
            return false;
        }
    }

    public String extractSubject(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
