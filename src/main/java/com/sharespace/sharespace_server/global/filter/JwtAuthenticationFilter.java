package com.sharespace.sharespace_server.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharespace.sharespace_server.global.exception.CustomException;
import com.sharespace.sharespace_server.global.exception.CustomRuntimeException;
import com.sharespace.sharespace_server.global.exception.error.JwtException;
import com.sharespace.sharespace_server.jwt.domain.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.PatternMatchUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.sharespace.sharespace_server.global.utils.RequestParser.extractUserIdFromToken;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    private final String[] whiteListUris = new String[] {"/**"};
//            {"/login",
//            "/user/login", "user/register"};

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (whiteListCheck(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

//        String authorizationHeader = request.getHeader(AUTHORIZATION);
//
//        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
//            String jwt = extractAccessToken(request);
//            try {
//                Authentication authentication = jwtProvider.getAuthentication(jwt);
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//            } catch (Exception e) {
//                sendJwtExceptionResponse(response, new CustomRuntimeException(JwtException.MALFORMED_JWT_EXCEPTION));
//                return;
//            }
//        }
//        String token = extractAccessToken(request);
//        request.setAttribute("userId", extractUserIdFromToken(jwtProvider.getClaims(token)));
//        filterChain.doFilter(request, response);
//        // 블랙리스트 필터 2024-07-30

        String jwt = getJwtFromCookies(request, "accessToken");

        if (jwt != null) {
            try {
                // Claims 가져오기 (유효성 검증 포함)
                jwtProvider.getClaims(jwt); // 유효하지 않으면 예외 발생

                // Authentication 설정
                Authentication authentication = jwtProvider.getAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // userId 설정 (선택 사항)
                Long userId = extractUserIdFromToken(jwtProvider.getClaims(jwt));
                request.setAttribute("userId", userId);
            } catch (RuntimeException e) {
                sendJwtExceptionResponse(response, e);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendJwtExceptionResponse(ServletResponse response, RuntimeException e) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ((HttpServletResponse)response).setStatus(HttpStatus.UNAUTHORIZED.value());

        CustomException jwtException = JwtException.from(e);
        response.getWriter().write(
                objectMapper.writeValueAsString(
                        new CustomRuntimeException(jwtException).sendError().getBody()
                ));
    }

    private boolean whiteListCheck(String uri) {
        return PatternMatchUtils.simpleMatch(whiteListUris, uri);
    }

    private String getJwtFromCookies(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(cookieName)) {
                return cookie.getValue();
            }
        }
        return null;
    }

}
