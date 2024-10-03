package com.sharespace.sharespace_server.note.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.note.dto.NoteDetailResponse;
import com.sharespace.sharespace_server.note.dto.NoteRequest;
import com.sharespace.sharespace_server.note.dto.NoteResponse;
import com.sharespace.sharespace_server.note.dto.NoteSenderListResponse;
import com.sharespace.sharespace_server.note.service.NoteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/note")
@Slf4j
public class NoteController {
	private final NoteService noteService;

	@GetMapping
	public BaseResponse<List<NoteResponse>> getNote() {
		return noteService.getNote();
	}

	@PostMapping
	public  BaseResponse<String> createNote(@RequestBody NoteRequest noteRequest) {
		return noteService.createNote(noteRequest);
	}

	@DeleteMapping
	public BaseResponse<String> deleteNote(@RequestParam Long noteId) {
		return noteService.deleteNote(noteId);
	}

	@GetMapping("/noteDetail")
	public BaseResponse<NoteDetailResponse> getNoteDetail(@RequestParam Long noteId) {
		return noteService.getNoteDetail(noteId);
	}

	@GetMapping("/available")
	public BaseResponse<List<NoteSenderListResponse>> getSenderList() {
		return noteService.getSenderList();
	}
}
