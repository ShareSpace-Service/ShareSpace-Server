package com.sharespace.sharespace_server.global.exception;

import org.springframework.http.HttpStatus;

public interface CustomException {
    HttpStatus status();
    String message();
}
