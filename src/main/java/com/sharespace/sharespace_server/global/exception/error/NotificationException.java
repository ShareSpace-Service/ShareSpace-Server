package com.sharespace.sharespace_server.global.exception.error;

import com.sharespace.sharespace_server.global.exception.CustomException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum NotificationException implements CustomException {
    NOTIFCATION_NOT_FOUND(HttpStatus.BAD_REQUEST,"존재하지 않는 알림 ID입니다");

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
