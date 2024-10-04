package com.sharespace.sharespace_server.matching.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.matching.dto.request.MatchingCompleteStorageRequest;
import com.sharespace.sharespace_server.matching.dto.request.MatchingKeepRequest;
import com.sharespace.sharespace_server.matching.dto.response.MatchingShowKeepDetailResponse;
import com.sharespace.sharespace_server.matching.service.MatchingService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/matching")
public class MatchingController {
	private final MatchingService matchingService;
	@PostMapping("/keep")
	public BaseResponse<Void> keep(@Valid @RequestBody MatchingKeepRequest matchingKeepRequest) {
		return matchingService.keep(matchingKeepRequest);
	}

	@GetMapping("/keepDetail")
	public BaseResponse<MatchingShowKeepDetailResponse> showKeepDetail(@RequestParam("matchingId") @NotNull Long matchingId) {
		return matchingService.showKeepDetail(matchingId);
	}

	@PutMapping("/completeStorage")
	public BaseResponse<Void> completeStorage(@Valid @RequestBody MatchingCompleteStorageRequest matchingCompleteStorageRequest) {
		Long matchingId = matchingCompleteStorageRequest.getMatchingId();
		return matchingService.completeStorage(matchingId);
	}


}
