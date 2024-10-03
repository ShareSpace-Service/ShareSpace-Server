package com.sharespace.sharespace_server.note.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class NoteRequest {
	private Long receiverId;
	private String title;
	private String content;
}
