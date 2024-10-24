package com.sharespace.sharespace_server.global.exception.error;

import org.springframework.http.HttpStatus;

import com.sharespace.sharespace_server.global.exception.CustomException;

import lombok.AllArgsConstructor;

// task: 임시 enum class. 추후 삭제 예정.
@AllArgsConstructor
public enum ProductException implements CustomException {
    PRODUCT_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 물품입니다."),
    PRODUCT_NOT_MATCHED_TO_USER(HttpStatus.BAD_REQUEST, "해당 물품은 해당 사용자의 것이 아닙니다.");

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
