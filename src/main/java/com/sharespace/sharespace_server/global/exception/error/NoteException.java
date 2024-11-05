package com.sharespace.sharespace_server.global.exception.error;

import org.springframework.http.HttpStatus;

import com.sharespace.sharespace_server.global.exception.CustomException;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum NoteException implements CustomException {
	NOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "작성한 쪽지가 없습니다."),
	NOTE_FAIL_DELETE(HttpStatus.INTERNAL_SERVER_ERROR, "쪽지 삭제에 실패하였습니다."),
	SENDER_NOT_FOUND(HttpStatus.NOT_FOUND, "송산자를 찾을 수 없습니다"),
	NOTE_NOT_MATCHING(HttpStatus.FORBIDDEN, "쪽지를 보낼 수 없습니다.");

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
