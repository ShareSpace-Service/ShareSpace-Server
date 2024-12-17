package com.sharespace.sharespace_server.matching.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sharespace.sharespace_server.global.annotation.CheckPermission;
import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.global.utils.RequestParser;
import com.sharespace.sharespace_server.matching.dto.response.MatchingHistoryResponse;
import com.sharespace.sharespace_server.matching.dto.response.MatchingShowKeepDetailResponse;
import com.sharespace.sharespace_server.matching.service.MatchingHistoryService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/history")
public class MatchingHistoryController {
	private final MatchingHistoryService matchingHistoryService;

	@GetMapping
	@CheckPermission(roles = {"ROLE_GUEST", "ROLE_HOST"})
	public BaseResponse<List<MatchingHistoryResponse>> getHistory(HttpServletRequest httpRequest) {
		Long userId = RequestParser.extractUserId(httpRequest);
		return matchingHistoryService.getHistory(userId);
	}

	@GetMapping("/detail")
	@CheckPermission(roles = {"ROLE_GUEST", "ROLE_HOST"})
	public BaseResponse<MatchingShowKeepDetailResponse> getHistoryDetail(@RequestParam("matchingId") Long matchingId) {
		return matchingHistoryService.getHistoryDetail(matchingId);
	}

}

