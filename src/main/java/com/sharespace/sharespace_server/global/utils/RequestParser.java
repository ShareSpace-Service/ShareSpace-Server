package com.sharespace.sharespace_server.global.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;

public class RequestParser {
    public static final String USER_ID = "userId";
    public static final String AUTHORIZATION = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    public static String extractAccessToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
            return authorizationHeader.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    public static Long extractUserId(HttpServletRequest request) {
        Object attribute = request.getAttribute(USER_ID);
        if (attribute == null) {
            return null;
        }

        String userId = attribute.toString();
        return Long.valueOf(userId);
    }

    public static Long extractUserIdFromToken(Claims claims) {

        String userId = String.valueOf(claims.get("userId"));
        return Long.valueOf(userId);
    }
    public static String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String roles = authentication.getAuthorities().toString();

        return roles.replace("[", "").replace("]","");
    }
}
