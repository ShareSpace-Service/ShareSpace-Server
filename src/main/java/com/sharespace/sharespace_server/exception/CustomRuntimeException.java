package com.sharespace.sharespace_server.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomRuntimeException extends RuntimeException {
    private static final String STATUS ="STATUS";
    private static final String MESSAGE ="MESSAGE";

    private final CustomException customException;
    
    public ResponseEntity<Map<String, String>> sendError() {
        Map<String, String> response = new LinkedHashMap<>();
        response.put(STATUS, customException.status().toString());
        response.put(MESSAGE, customException.message());
        return ResponseEntity.status(customException.status()).body(response);
    }
}
