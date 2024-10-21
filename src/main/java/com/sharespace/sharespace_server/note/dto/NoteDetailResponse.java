package com.sharespace.sharespace_server.note.dto;

import java.time.format.DateTimeFormatter;

import com.sharespace.sharespace_server.note.entity.Note;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class NoteDetailResponse {
	private Long noteId;
	private String title;
	private String content;
	private String sender;
	private String senderTime;

	public static NoteDetailResponse from(Note note) {
		return NoteDetailResponse.builder()
			.noteId(note.getId())
			.title(note.getTitle())
			.content(note.getContent())
			.sender(note.getSender().getNickName())
			.senderTime(note.getSend_at().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
			.build();
	}
}
