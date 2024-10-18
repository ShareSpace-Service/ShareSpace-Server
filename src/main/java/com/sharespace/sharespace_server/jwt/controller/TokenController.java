package com.sharespace.sharespace_server.jwt.controller;

import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.jwt.service.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
public class TokenController {

    private final JwtService jwtService;

    @PostMapping("/reissue")
    public BaseResponse<HttpStatus> reissueAccessToken(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response) {
        return jwtService.reissueAccessToken(refreshToken, response);
    }
}
