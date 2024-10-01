package com.sharespace.sharespace_server.exception.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sharespace.sharespace_server.exception.CustomRuntimeException;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class BaseExceptionHandler {
    @ExceptionHandler(CustomRuntimeException.class)
    public ResponseEntity customHandler(CustomRuntimeException e) {
        log.error("api 예외발생! " + e.getCustomException().message());
        return e.sendError();
    }
}
