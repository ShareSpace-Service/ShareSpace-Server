package com.sharespace.sharespace_server.global.exception.handler;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sharespace.sharespace_server.global.exception.CustomRuntimeException;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class BaseExceptionHandler {
    @ExceptionHandler(CustomRuntimeException.class)
    public ResponseEntity customHandler(CustomRuntimeException e) {
        log.error("api 예외발생! " + e.getCustomException().message());
        return e.sendError();
    }

    // @Valid 유효성 검사시 실패 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> response = new LinkedHashMap<>();

        String defaultMessage = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        response.put("STATUS", "400 BAD_REQUEST");
        response.put("MESSAGE", defaultMessage);  // 유효성 검사 실패 메시지

        return ResponseEntity.badRequest().body(response);
    }

}
