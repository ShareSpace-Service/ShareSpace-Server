package com.sharespace.sharespace_server.common.exception;

import org.springframework.http.HttpStatus;

public interface CustomException {
    HttpStatus status();
    String message();
}
