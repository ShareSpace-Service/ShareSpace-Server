package com.sharespace.sharespace_server.jwt.entity;

import com.sharespace.sharespace_server.jwt.domain.Jwt;
import com.sharespace.sharespace_server.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Getter
@Table(name = "token", schema = "sharespace")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

    @Builder
    public Token(User user, String refreshToken) {
        this.id = id;
        this.user = user;
        this.refreshToken = refreshToken;
    }

    public static Token of(User user, Jwt jwt) {
        return Token.builder()
                .user(user)
                .refreshToken(jwt.getRefreshToken())
                .build();
    }
}
