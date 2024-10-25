package com.sharespace.sharespace_server.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserGetIdResponse {
	Long userId;
}
