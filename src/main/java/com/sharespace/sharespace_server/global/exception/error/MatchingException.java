package com.sharespace.sharespace_server.global.exception.error;

import org.springframework.http.HttpStatus;

import com.sharespace.sharespace_server.global.exception.CustomException;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MatchingException implements CustomException {
	MATCHING_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 매칭입니다."),
	CATEGORY_NOT_MATCHED(HttpStatus.BAD_REQUEST, "Category에 알맞은 물품을 보관 요청해야합니다."),
	MATCHING_PRODUCT_NOT_IN_USER(HttpStatus.BAD_REQUEST, "해당 매칭의 Product를 가지고 있지 않는 유저입니다."),
	MATCHING_PLACE_NOT_IN_USER(HttpStatus.BAD_REQUEST, "해당 매칭의 Place를 가지고 있지 않는 유저입니다."),
	GUEST_ALREADY_COMPLETED_KEEPING(HttpStatus.BAD_REQUEST, "Guest는 이미 보관처리를 완료하였습니다."),
	HOST_ALREADY_COMPLETED_KEEPING(HttpStatus.BAD_REQUEST, "Guest는 이미 보관처리를 완료하였습니다."),
	INCORRECT_STATUS_CONFIRM_REQUEST_GUEST(HttpStatus.BAD_REQUEST, "Guest가 물품 보관을 수락하려면 Status가 '보관 대기중(Pending)' 이어야합니다."),
	INVALID_PRODUCT_PERIOD(HttpStatus.BAD_REQUEST, "Product의 보관 기간은 Place보다 클 수 없습니다."),
	REQUEST_CANCELLATION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "상태가 보관 대기중(Pending)이 아니어서 보관 요청 취소를 할 수 없습니다."),
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
