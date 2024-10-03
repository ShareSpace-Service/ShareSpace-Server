package com.sharespace.sharespace_server.note.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class NoteResponse {
	private Long noteId;
	private String title;
	private String content;
	private String sender;
}