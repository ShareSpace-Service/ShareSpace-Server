package com.sharespace.sharespace_server.global.exception.error;

import org.springframework.http.HttpStatus;

import com.sharespace.sharespace_server.global.exception.CustomException;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum PlaceException implements CustomException {
	PLACE_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 장소입니다."),
	PLACE_REGISTRATION_FAILED(HttpStatus.BAD_REQUEST, "장소 등록에 실패하였습니다."),
	PLACE_REQUIRED_FIELDS_EMPTY(HttpStatus.BAD_REQUEST, "필수 입력 항목이 누락되었습니다. 모든 필드를 확인해 주세요.");


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
