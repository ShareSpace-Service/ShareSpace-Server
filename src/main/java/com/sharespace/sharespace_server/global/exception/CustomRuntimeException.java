package com.sharespace.sharespace_server.global.exception;
import org.springframework.http.ResponseEntity;

import com.sharespace.sharespace_server.global.response.BaseResponse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomRuntimeException extends RuntimeException {
    private static final String STATUS ="status";
    private static final String MESSAGE ="message";
    private final CustomException customException;

    public ResponseEntity<BaseResponse<String>> sendError() {
        // Map<String, String> response = new LinkedHashMap<>();
        return ResponseEntity
            .status(customException.status())
            .body(BaseResponse.error(customException.message(), customException.status()));
    }

}
