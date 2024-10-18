package com.sharespace.sharespace_server.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharespace.sharespace_server.jwt.domain.Jwt;
import com.sharespace.sharespace_server.jwt.service.JwtService;
import com.sharespace.sharespace_server.user.entity.User;
import com.sharespace.sharespace_server.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final JwtService jwtService;
    private final UserService userService;

    public LoginFilter(AuthenticationManager authenticationManager, JwtService jwtService, UserService userService) {
        setAuthenticationManager(authenticationManager); // AuthenticationManager 설정
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws
            AuthenticationException {

        // 클라이언트 요청에서 email, password 추출
        String email = obtainUsername(request);
        String password = obtainPassword(request);

        if (!userService.checkAccountLocked(email)) {
            // 해당 멤버의 계정이 잠겨있는지 확인하고, 계정이 잠겨있지 않으면 인증 토큰을 만든다.
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password,
                    null);
            return getAuthenticationManager().authenticate(authToken);
        } else {
            // 계정이 잠겨있는 상태면, 423 에러(HttpStatus.LOCKED)를 반환한다.
            try {
                sendUserLoginResponse(response, HttpStatus.LOCKED);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        throw new AuthenticationException("계정이 잠겨있습니다.") {
        };

    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException {
        User user = (User)authResult.getPrincipal();
        Jwt token = jwtService.createTokens(user.getId(), user);

        // AccessToken 쿠키 저장
        addJwtToCookie(response, "accessToken", token.getAccessToken(), 3600); // 1시간
        // RefreshToken 쿠키 저장
        addJwtToCookie(response, "refreshToken", token.getRefreshToken(), 60 * 60 * 24 * 60);  // 60일

        userService.loginAttemptationSuccess(obtainUsername(request));
        Collection<? extends GrantedAuthority> authorities = authResult.getAuthorities();

        sendUserLoginResponse(response, HttpStatus.OK);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        userService.loginAttemptationFailed(obtainUsername(request));
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("utf-8");
        response.getWriter().write("인증 실패: " + failed.getMessage());
    }

    private void sendUserLoginResponse(HttpServletResponse response, HttpStatus status) throws IOException {
        Map<String, String> messageMap = new HashMap<>();
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status.value()); // HttpStatus.OK.value();
        PrintWriter out = response.getWriter();
        out.print(new ObjectMapper().writeValueAsString(messageMap));
        out.flush();
    }

    // 로그인 성공시 JWT를 쿠키에 저장한다.
    public void addJwtToCookie(HttpServletResponse response, String jwtToken, String cookieName, int maxAge) {
        Cookie cookie = new Cookie(cookieName, jwtToken);
        cookie.setHttpOnly(false);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge); // 2시간
        response.addCookie(cookie);
    }
}
