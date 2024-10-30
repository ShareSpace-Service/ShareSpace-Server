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
import com.sharespace.sharespace_server.global.utils.RequestParser;
import com.sharespace.sharespace_server.note.dto.NoteDetailResponse;
import com.sharespace.sharespace_server.note.dto.NoteRequest;
import com.sharespace.sharespace_server.note.dto.NoteResponse;
import com.sharespace.sharespace_server.note.dto.NoteSenderListResponse;
import com.sharespace.sharespace_server.note.service.NoteService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/note")
@Slf4j
public class NoteController {
	private final NoteService noteService;

	// task: 받은 쪽지 전체 조회
	@GetMapping
	public BaseResponse<List<NoteResponse>> getNote(HttpServletRequest httpRequest) {
		Long userId = RequestParser.extractUserId(httpRequest);
		return noteService.getAllNotes(userId);
	}

	// task: 쪽지 전송
	@PostMapping
	public  BaseResponse<String> createNote(@Valid @RequestBody NoteRequest noteRequest, HttpServletRequest httpRequest) {
		Long userId = RequestParser.extractUserId(httpRequest);
		return noteService.createNote(noteRequest, userId);
	}

	// task: 쪽지 삭제
	@DeleteMapping
	public BaseResponse<String> deleteNote(@RequestParam Long noteId) {
		return noteService.deleteNote(noteId);
	}

	// task: 상세 쪽지 내용 조회
	@GetMapping("/noteDetail")
	public BaseResponse<NoteDetailResponse> getNoteDetail(@RequestParam Long noteId) {
		return noteService.getNoteDetail(noteId);
	}

	// task: 수신자 대상 조회
	@GetMapping("/available")
	public BaseResponse<List<NoteSenderListResponse>> getSenderList(HttpServletRequest httpRequest) {
		Long userId = RequestParser.extractUserId(httpRequest);
		return noteService.getSenderList(userId);
	}
}
