package com.sharespace.sharespace_server.note.dto;

import lombok.Getter;

@Getter
public class NoteUnreadCountResponse {
	private final int unreadCount;

	public NoteUnreadCountResponse(int unreadCount) {
		this.unreadCount = unreadCount;
	}
}
