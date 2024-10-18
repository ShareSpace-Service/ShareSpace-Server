package com.sharespace.sharespace_server.jwt.service;

import com.sharespace.sharespace_server.global.exception.CustomRuntimeException;
import com.sharespace.sharespace_server.global.exception.error.JwtException;
import com.sharespace.sharespace_server.global.exception.error.UserException;
import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.global.response.BaseResponseService;
import com.sharespace.sharespace_server.jwt.domain.Jwt;
import com.sharespace.sharespace_server.jwt.domain.JwtProvider;
import com.sharespace.sharespace_server.jwt.entity.Token;
import com.sharespace.sharespace_server.jwt.repository.TokenJpaRepository;
import com.sharespace.sharespace_server.user.entity.User;
import com.sharespace.sharespace_server.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final TokenJpaRepository tokenJpaRepository;
    private final BaseResponseService baseResponseService;

    @Transactional
    public Jwt createTokens(Long userId, User user) {
        validateMember(userId);
        Jwt jwt = jwtProvider.generateJwtPair(Collections.singletonMap("userId", userId));
        Token token = Token.of(user, jwt);
        if (tokenJpaRepository.findByUserId(userId).isPresent()) {
            tokenJpaRepository.deleteById(userId);
        }
        tokenJpaRepository.save(token);
        return jwt;
    }

    public BaseResponse<HttpStatus> reissueAccessToken(String refreshToken, HttpServletResponse response) {

        try {
            // refreshToken 유효성 검증
            Claims claims = jwtProvider.getClaims(refreshToken);

            // refreshToken에 포함된 userId로 새로운 accessToken 생성
            Long userId = (Long) claims.get("userId");
            User user = tokenJpaRepository.findByUserId(userId)
                    .orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND)).getUser();

            String newAccessToken = jwtProvider.reissueAccessToken(Collections.singletonMap("userId", userId));

            // 새로운 accessToken을 쿠키에 저장
            addJwtToCookie(response, "accessToken", newAccessToken, 3600);  // 예: 1시간

            return baseResponseService.getSuccessResponse(HttpStatus.OK);

        } catch (CustomRuntimeException e) {
            return baseResponseService.getSuccessResponse(HttpStatus.UNAUTHORIZED);
        }
    }

    private void validateMember(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));
    }

    private void addJwtToCookie(HttpServletResponse response, String tokenName, String token, int maxAge) {
        Cookie cookie = new Cookie(tokenName, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
}
