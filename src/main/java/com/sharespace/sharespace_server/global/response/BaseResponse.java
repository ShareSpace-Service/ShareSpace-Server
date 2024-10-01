package com.sharespace.sharespace_server.global.response;

import org.springframework.http.HttpStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BaseResponse<T> {
	private boolean isSuccess; // 성공, 실패여부
	private String message; // 메시지
	private HttpStatus status; // 상태 코드
	private T data; // 전달 데이터
	@Builder
	public BaseResponse(boolean isSuccess, String message, HttpStatus status, T data) {
		this.isSuccess = isSuccess;
		this.message = message;
		this.status = status;
		this.data = data;
	}

}
