package com.sharespace.sharespace_server.jwt.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sharespace.sharespace_server.jwt.service.TokenBlacklistService;
import com.sharespace.sharespace_server.user.entity.User;
import com.sharespace.sharespace_server.user.service.CustomUserDetailService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.sharespace.sharespace_server.global.utils.RequestParser.extractUserIdFromToken;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final TokenBlacklistService tokenBlacklistService;
    private final CustomUserDetailService customUserDetailService;

    @Value("${jwt.token.secret-key}")
    private String signature;
    private byte[] secret;
    private Key key;

    @PostConstruct
    public void setSecretKey() {
        secret = signature.getBytes();
        key = Keys.hmacShaKeyFor(secret);
    }

    private String createToken(Map<String, Object> claims, Date exprireDate) {
        long currentTimeMillis = System.currentTimeMillis();
        Map<String, Object> modifiableClaims = new HashMap<>(claims);
        modifiableClaims.put("createdMills", currentTimeMillis);

        Map<String, Object> immutableClaims = Collections.unmodifiableMap(modifiableClaims);

        return Jwts.builder()
                .setClaims(immutableClaims)
                .setExpiration(exprireDate)
                .signWith(key)
                .compact();
    }

    public Claims getClaims(String token) {
        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            throw new JwtException("Token is blacklisted");
        }
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Authentication getAuthentication(String token) throws JsonProcessingException {

        Long userId = extractUserIdFromToken(getClaims(token));
        User user = customUserDetailService.findUserByUserId(userId);
        UserDetails userDetails = customUserDetailService.loadUserByUsername(user.getEmail());

        if (userDetails == null) {
            // TODO : Spring Security에서 UserDetails가 null일 때 예외찾기
            return null;
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails.getUsername(),
                null, userDetails.getAuthorities());
        return authentication;
    }

    public Jwt generateJwtPair(Map<String, Object> claims) {
        String accessToken = createToken(claims, getExpireDateAccessToken());
        String refreshToken = createToken(new HashMap<>(), getExpireDateRefreshToken());
        return new Jwt(accessToken, refreshToken);
    }

    private Date getExpireDateAccessToken() {
        long expireTimeMils = 1000L * 60 * 360;
        return new Date(System.currentTimeMillis() + expireTimeMils);
    }

    private Date getExpireDateRefreshToken() {
        long expireTimeMils = 1000L * 60 * 60 * 24 * 60;
        return new Date(System.currentTimeMillis() + expireTimeMils);
    }

    public String reissueAccessToken(Map<String, Object> claims) {
        return createToken(claims, getExpireDateAccessToken());
    }
}
