package com.sharespace.sharespace_server.jwt.service;

import com.sharespace.sharespace_server.global.exception.CustomRuntimeException;
import com.sharespace.sharespace_server.global.exception.error.UserException;
import com.sharespace.sharespace_server.jwt.domain.Jwt;
import com.sharespace.sharespace_server.jwt.domain.JwtProvider;
import com.sharespace.sharespace_server.jwt.entity.Token;
import com.sharespace.sharespace_server.jwt.repository.TokenJpaRepository;
import com.sharespace.sharespace_server.user.entity.User;
import com.sharespace.sharespace_server.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final TokenJpaRepository tokenJpaRepository;

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

    private void validateMember(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));
    }
}
