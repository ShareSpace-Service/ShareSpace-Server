package com.sharespace.sharespace_server.global.response;

import org.springframework.http.HttpStatus;

public interface BaseResponseService {
	/**
	 * 성공 응답 메서드 - 전달 데이터 O
	 *
	 * @param data - 결과 데이터
	 * @param <T> 반환 타입 => Generic
	 * @return BaseResponse - 응답 객체
	 */
	<T> BaseResponse<Object> getSuccessResponse(T data);

	/**
	 * 성공 응답 메서드 - 전달 데이터 X
	 *
	 * @param <T> 반환 타입 => Generic
	 * @return BaseResponse - 응답 객체
	 */
	<T> BaseResponse<Object> getSuccessResponse();

	/**
	 * 실패 응답 메서드의 경우, CustomException 핸들러를 활용한다.
	 */

}
