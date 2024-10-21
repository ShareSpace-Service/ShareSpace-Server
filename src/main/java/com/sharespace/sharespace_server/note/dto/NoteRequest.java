package com.sharespace.sharespace_server.note.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class NoteRequest {
	@Positive
	@NotNull(message = "수신자를 선택해주세요")
	private Long receiverId;
	@NotEmpty
	@NotNull(message = "제목을 입력해주세요")
	private String title;
	@NotEmpty
	@NotNull(message = "내용을 입력해주세요")
	private String content;
}
