package com.sharespace.sharespace_server.matching.controller;

import com.sharespace.sharespace_server.matching.dto.request.MatchingGuestConfirmStorageRequest;
import com.sharespace.sharespace_server.matching.dto.request.MatchingHostAcceptRequestRequest;
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
import com.sharespace.sharespace_server.matching.dto.response.MatchingShowRequestDetailResponse;
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
	public BaseResponse<Void> keep(@Valid @RequestBody MatchingKeepRequest request) {
		return matchingService.keep(request);
	}

	@GetMapping("/keepDetail")
	public BaseResponse<MatchingShowKeepDetailResponse> showKeepDetail(@RequestParam("matchingId") @NotNull Long matchingId) {
		return matchingService.showKeepDetail(matchingId);
	}

	@PutMapping("/completeStorage")
	public BaseResponse<Void> completeStorage(@Valid @RequestBody MatchingCompleteStorageRequest request) {
		Long matchingId = request.getMatchingId();
		return matchingService.completeStorage(matchingId);
	}

	@GetMapping("/requestDetail")
	public BaseResponse<MatchingShowRequestDetailResponse> showRequestDetail(@RequestParam("matchingId") @NotNull Long matchingId) {
		return matchingService.showRequestDetail(matchingId);
	}

	@PostMapping("/acceptRequest/host")
	public BaseResponse<Void> hostAcceptRequest(@Valid @RequestBody MatchingHostAcceptRequestRequest request) {
		return matchingService.hostAcceptRequest(request);
	}

	@PutMapping("/confirmStorage/guest")
	public BaseResponse<Void> guestConfirmStorage(@Valid @RequestBody MatchingGuestConfirmStorageRequest request) {
		return matchingService.guestConfirmStorage(request);

	}

	@PostMapping("/cancelRequest")
	public BaseResponse<Void> cancelRequest(@RequestParam("matchingId") Long matchingId) {
		return matchingService.cancelRequest(matchingId);
	}
}

