package com.sharespace.sharespace_server.note.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class NoteUserResponse {
	private Long receiverId;
	private String nickname;
}
