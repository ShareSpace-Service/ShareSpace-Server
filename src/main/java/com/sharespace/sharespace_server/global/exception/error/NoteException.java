package com.sharespace.sharespace_server.global.exception.error;

import org.springframework.http.HttpStatus;

import com.sharespace.sharespace_server.global.exception.CustomException;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum NoteException implements CustomException {
	NOTE_NOT_FOUND(HttpStatus.BAD_REQUEST, "작성한 쪽지가 없습니다."),
	NOTE_FAIL_DELETE(HttpStatus.BAD_REQUEST, "쪽지 삭제에 실패하였습니다."),
	NOTE_TITLE_ANE_CONTENT_EMPTY(HttpStatus.BAD_REQUEST, "내용을 입력해주세요"),
	RECEIVER_NOT_FOUND(HttpStatus.BAD_REQUEST, "수신자가 입력되지 않았습니다. 수신자를 입력해 주세요 "),
	SENDER_NOT_FOUND(HttpStatus.BAD_REQUEST, "송산자를 찾을 수 없습니다");

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
