package com.sharespace.sharespace_server.jwt.repository;

import com.sharespace.sharespace_server.jwt.entity.Token;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TokenJpaRepository extends JpaRepository<Token, Long> {
//    @Modifying
//    @Transactional
//    @Query("DELETE FROM Token t WHERE t.user.id = :userId")
//    void deleteByUserId(Long userId);

    Optional<Token> findByUserId(Long userId);

    Optional<Token> findByRefreshToken(String refreshToken);

    void deleteById(Long userId);
}
