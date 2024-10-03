package com.sharespace.sharespace_server.global.exception.error;

import com.sharespace.sharespace_server.global.exception.CustomException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum LocationException implements CustomException {
    FETCH_FAIL(HttpStatus.BAD_REQUEST, "카카오 API에서 좌표를 가져오지 못했습니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus status() {
        return this.status;
    }

    @Override
    public String message() {
        return this.message;
    }
}
