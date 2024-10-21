package com.sharespace.sharespace_server.note.dto;

import com.sharespace.sharespace_server.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class NoteSenderListResponse {
	private Long receiverId;
	private String nickname;

	public static NoteSenderListResponse toNoteSenderListResponse(User user) {
		return NoteSenderListResponse.builder()
			.receiverId(user.getId())
			.nickname(user.getNickName())
			.build();
	}
}
