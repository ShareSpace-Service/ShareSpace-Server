package com.sharespace.sharespace_server.global.exception.error;

import org.springframework.http.HttpStatus;

import com.sharespace.sharespace_server.global.exception.CustomException;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum PlaceException implements CustomException {
	PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 장소입니다."),
	PLACE_REGISTRATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "장소 등록에 실패하였습니다."),
	PLACE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 사용자에 대한 장소가 존재합니다.");

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
