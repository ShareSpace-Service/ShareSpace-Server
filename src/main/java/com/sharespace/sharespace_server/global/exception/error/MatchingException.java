package com.sharespace.sharespace_server.global.exception.error;

import org.springframework.http.HttpStatus;

import com.sharespace.sharespace_server.global.exception.CustomException;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MatchingException implements CustomException {
	CANNOT_FIND_MATCHING(HttpStatus.BAD_REQUEST, "존재하지 않는 매칭입니다."),
	CATEGORY_NOT_MATCHED(HttpStatus.BAD_REQUEST, "Category에 알맞은 물품을 보관 요청해야합니다."),
	ALREADY_EXISTED_MATCHING(HttpStatus.BAD_REQUEST, "이미 존재하는 매칭입니다.");

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
