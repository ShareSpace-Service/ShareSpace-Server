package com.sharespace.sharespace_server.global.response;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class BaseResponseServiceImpl implements BaseResponseService {
	@Override
	public <T> BaseResponse<T> getSuccessResponse(T data) {
		return BaseResponse.<T>builder()
			.isSuccess(true)
			.status(HttpStatus.OK)
			.message(BaseResponseStatus.SUCCESS.getMessage())
			.data(data)
			.build();
	}

	@Override
	public <T> BaseResponse<T> getSuccessResponse() {
		return BaseResponse.<T>builder()
			.isSuccess(true)
			.status(HttpStatus.OK)
			.message(BaseResponseStatus.SUCCESS.getMessage())
			.build();
	}

}
