package com.sharespace.sharespace_server.exception;

import org.springframework.http.HttpStatus;

public interface CustomException {
    HttpStatus status();
    String message();
}
