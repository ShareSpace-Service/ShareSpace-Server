package com.sharespace.sharespace_server.jwt.domain;

import lombok.Getter;

@Getter
public class Jwt {
    private String accessToken;
    private String refreshToken;

    public Jwt(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
