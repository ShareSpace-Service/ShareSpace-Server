package com.sharespace.sharespace_server.note.dto;

import com.sharespace.sharespace_server.note.entity.Note;

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
	private boolean isRead;

	public static NoteResponse toNoteResponse(Note note) {
		return NoteResponse.builder()
			.noteId(note.getId())
			.title(note.getTitle())
			.content(note.getContent())
			.sender(note.getSender().getNickName())
			.isRead(note.isRead())
			.build();
	}
}
