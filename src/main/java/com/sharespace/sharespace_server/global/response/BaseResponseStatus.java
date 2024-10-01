package com.sharespace.sharespace_server.global.response;

import java.security.PrivilegedAction;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BaseResponseStatus {
	SUCCESS("요청에 성공하였습니다");

	private final String message;
}
