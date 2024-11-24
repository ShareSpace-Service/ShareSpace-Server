package com.sharespace.sharespace_server.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserEmailValidateResponse {
	private String email;
}
