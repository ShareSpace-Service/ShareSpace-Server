package com.sharespace.sharespace_server.global.exception.error;

import org.springframework.http.HttpStatus;

import com.sharespace.sharespace_server.global.exception.CustomException;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ImageException implements CustomException {
	IMAGE_UPLOAD_FAIL(HttpStatus.BAD_REQUEST, "이미지를 업로드하는 중 오류가 발생했습니다."),
	IMAGE_UPDATE_FAIL(HttpStatus.BAD_REQUEST, "이미지를 업로드하는 중 오류가 발생했습니다."),
	IMAGE_DELETE_FAIL(HttpStatus.BAD_REQUEST, "이미지를 삭제하는 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");

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