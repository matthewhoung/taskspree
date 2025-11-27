package com.hongsolo.taskspree.modules.identity.infrastructure.security;

import com.hongsolo.taskspree.modules.identity.application.services.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider implements TokenProvider {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    public String generateAccessToken(UUID identityId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getAccessExpiration());

        return Jwts.builder()
                .subject(identityId.toString())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    @Override
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    public UUID extractIdentityId(String token) {
        try {
            Claims claims = parseToken(token);
            return UUID.fromString(claims.getSubject());
        } catch (Exception e) {
            log.debug("Failed to extract identity ID from token: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean validateAccessToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (SignatureException e) {
            log.debug("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.debug("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.debug("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.debug("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public long getAccessTokenExpirationMs() {
        return jwtProperties.getAccessExpiration();
    }

    @Override
    public long getRefreshTokenExpirationMs() {
        return jwtProperties.getRefreshExpiration();
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}