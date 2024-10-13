package com.sharespace.sharespace_server.global.exception.error;

import org.springframework.http.HttpStatus;

import com.sharespace.sharespace_server.global.exception.CustomException;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ImageException implements CustomException {
	IMAGE_UPLOAD_FAIL(HttpStatus.BAD_REQUEST, "이미지를 업로드하는 중 오류가 발생했습니다."),
	IMAGE_UPDATE_FAIL(HttpStatus.BAD_REQUEST, "이미지를 업로드하는 중 오류가 발생했습니다."),
	IMAGE_DELETE_FAIL(HttpStatus.BAD_REQUEST, "이미지를 삭제하는 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."),
	IMAGE_GET_FAIL(HttpStatus.BAD_REQUEST, "이미지를 불러오는 과정에서 오류가 발생하였습니다."),
	INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "허용되지 않은 파일 확장자입니다."),
	IMAGE_NOT_EXCEPTION(HttpStatus.BAD_REQUEST, "확장자를 찾을 수 없습니다."),
	IMAGE_REQUIRED_FIELDS_EMPTY(HttpStatus.BAD_REQUEST, "이미지가 누락되었습니다.");

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