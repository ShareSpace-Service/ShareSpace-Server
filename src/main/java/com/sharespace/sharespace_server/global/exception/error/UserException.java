package com.sharespace.sharespace_server.global.exception.error;

import org.springframework.http.HttpStatus;

import com.sharespace.sharespace_server.global.exception.CustomException;

import lombok.AllArgsConstructor;

// task: 임시 enum class. 추후 삭제 예정.
@AllArgsConstructor
public enum UserException implements CustomException {
    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "이미 발급된 이메일입니다."),
    EMAIL_VALIDATION_FAIL(HttpStatus.CONFLICT, "인증번호가 일치하지 않습니다."),
    NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 유저입니다."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "비밀번호 양식을 지켜주세요."),
    PASSWORD_VALIDATE_FAIL(HttpStatus.BAD_REQUEST, "비밀번호와 비밀번호 확인란에 기입된 값이 다릅니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 틀렸습니다."),
    EMAIL_SEND_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 전송에 실패하였습니다. 잠시 후 다시 시도해 주세요.");

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
