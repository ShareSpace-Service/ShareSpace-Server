package com.sharespace.sharespace_server.global.exception.error;

import com.sharespace.sharespace_server.global.exception.CustomException;
import com.sharespace.sharespace_server.global.exception.CustomRuntimeException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpStatus;

public enum JwtException implements CustomException {
    EXPIRED_JWT_EXCEPTION(HttpStatus.UNAUTHORIZED, "기한이 만료되었습니다."),
    MALFORMED_JWT_EXCEPTION(HttpStatus.UNAUTHORIZED, "잘못된 형식의 토큰입니다."),
    SIGNATURE_EXCEPTION(HttpStatus.UNAUTHORIZED, "올바른 키가 아닙니다."),
    // ILLEGAL_ARGUMENT_EXCEPTION(HttpStatus.UNAUTHORIZED, "잘못된 값이 들어왔습니다."),
    REFRESH_TOKEN_NOT_FOUND_EXCEPTION(HttpStatus.BAD_REQUEST, "DB에 Refresh token이 존재하지 않습니다."),
    BLACKLISTED_JWT_EXCEPTION(HttpStatus.UNAUTHORIZED, "블랙리스트에 등록된 토큰입니다."),
    MISSING_HEADER_TOKEN(HttpStatus.UNAUTHORIZED, "Header에 토큰이 존재하지 않습니다.");
    private final HttpStatus httpStatus;
    private final String message;

    JwtException(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public static CustomException from(RuntimeException e) {
        if (e.getClass().equals(ExpiredJwtException.class)) {
            return JwtException.EXPIRED_JWT_EXCEPTION;
        }
        if (e.getClass().equals(MalformedJwtException.class)) {
            return JwtException.MALFORMED_JWT_EXCEPTION;
        }
        if (e.getClass().equals(SignatureException.class)) {
            return JwtException.SIGNATURE_EXCEPTION;
        }
        // if (e.getClass().equals(IllegalArgumentException.class)) {
        // 	return JwtException.ILLEGAL_ARGUMENT_EXCEPTION;
        // }
        return ((CustomRuntimeException)e).getCustomException();
    }

    @Override
    public HttpStatus status() {
        return httpStatus;
    }

    @Override
    public String message() {
        return message;
    }
}
