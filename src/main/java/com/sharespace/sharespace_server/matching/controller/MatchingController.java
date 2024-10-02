package com.sharespace.sharespace_server.matching.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.global.response.BaseResponseService;
import com.sharespace.sharespace_server.matching.dto.request.MatchingRequestDto;
import com.sharespace.sharespace_server.matching.service.MatchingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/matching")
public class MatchingController {
	private final BaseResponseService baseResponseService;
	private final MatchingService matchingService;
	@PostMapping("/keep")
	public BaseResponse<Void> keep(@RequestBody MatchingRequestDto matchingRequestDto) {
		matchingService.keep(matchingRequestDto);
		return baseResponseService.getSuccessResponse();
	}

	@GetMapping("/keepDetail")
	public BaseResponse<Void> showKeepDetail(@RequestParam("matchingId") Long matchingId) {
		return baseResponseService.getSuccessResponse();
	}


}
