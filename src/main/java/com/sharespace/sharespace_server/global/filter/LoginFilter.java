package com.sharespace.sharespace_server.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharespace.sharespace_server.global.exception.CustomRuntimeException;
import com.sharespace.sharespace_server.global.exception.error.UserException;
import com.sharespace.sharespace_server.jwt.domain.Jwt;
import com.sharespace.sharespace_server.jwt.entity.Token;
import com.sharespace.sharespace_server.jwt.repository.TokenJpaRepository;
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

        // 해당 이메일 기반 유저 존재 여부 확인
        if(!userService.checkUserPresents(email)) {
            try {
                sendErrorResponse(response, HttpStatus.BAD_REQUEST, "존재하지 않는 계정입니다.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        // 이메일 인증여부 확인
        if(!userService.loginAttemptaionCheck(email)) {
            try {
                sendErrorResponse(response, HttpStatus.BAD_REQUEST, "이메일 인증이 되어 있지 않은 사용자입니다.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        // 계정 잠금여부 확인
        if (!userService.checkAccountLocked(email)) {
            // 해당 멤버의 계정이 잠겨있는지 확인하고, 계정이 잠겨있지 않으면 인증 토큰을 만든다.
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password,
                    null);
            return getAuthenticationManager().authenticate(authToken);
        } else {
            // 계정이 잠겨있는 상태면, 423 에러(HttpStatus.LOCKED)를 반환한다.
            try {
                sendErrorResponse(response, HttpStatus.LOCKED, "계정이 잠겨있습니다.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        }

    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException {
        User user = (User)authResult.getPrincipal();
        Jwt token = jwtService.checkUserToken(user);

        // AccessToken 쿠키 저장
        addJwtToCookie(response, token.getAccessToken(), "accessToken",3600); // 1시간
        // RefreshToken 쿠키 저장
        addJwtToCookie(response, token.getRefreshToken(), "refreshToken", 60 * 60 * 24 * 60);  // 60일

        userService.loginAttemptationSuccess(obtainUsername(request));

        sendUserLoginResponseWithUserId(response, HttpStatus.OK, user.getId());
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        userService.loginAttemptationFailed(obtainUsername(request));
        sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "비밀번호가 틀렸습니다.");
    }

    private void sendUserLoginResponse(HttpServletResponse response, HttpStatus status) throws IOException {
        Map<String, String> messageMap = new HashMap<>();
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status.value()); // HttpStatus.OK.value();
        PrintWriter out = response.getWriter();
        out.print(new ObjectMapper().writeValueAsString(messageMap));
        out.flush();
    }

    // userId를 추가로 전달하는 새로운 메서드
    private void sendUserLoginResponseWithUserId(HttpServletResponse response, HttpStatus status, Long userId) throws IOException {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("userId", userId); // userId를 응답에 추가

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status.value());
        PrintWriter out = response.getWriter();
        out.print(new ObjectMapper().writeValueAsString(messageMap));
        out.flush();
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status.value());
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("STATUS", status.toString());
        errorResponse.put("MESSAGE", message);

        PrintWriter out = response.getWriter();
        out.print(new ObjectMapper().writeValueAsString(errorResponse));
        out.flush();
    }

    // 로그인 성공시 JWT를 쿠키에 저장한다.
    public void addJwtToCookie(HttpServletResponse response, String jwtToken, String cookieName, int maxAge) {
        Cookie cookie = new Cookie(cookieName, jwtToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);

        String cookieHeader = String.format(
                "%s=%s; Max-Age=%d; Path=%s; Secure; HttpOnly; SameSite=Strict",
                cookieName,
                jwtToken,
                maxAge,
                "/"
        );

        response.addHeader("Set-Cookie", cookieHeader);
    }
}
