package com.sharespace.sharespace_server.matching.controller;

import com.sharespace.sharespace_server.matching.dto.request.MatchingGuestConfirmStorageRequest;
import com.sharespace.sharespace_server.matching.dto.request.MatchingHostAcceptRequestRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.matching.dto.request.MatchingCompleteStorageRequest;
import com.sharespace.sharespace_server.matching.dto.request.MatchingKeepRequest;
import com.sharespace.sharespace_server.matching.dto.request.MatchingUploadImageRequest;
import com.sharespace.sharespace_server.matching.dto.response.MatchingShowAllResponse;
import com.sharespace.sharespace_server.matching.dto.response.MatchingShowKeepDetailResponse;
import com.sharespace.sharespace_server.matching.dto.response.MatchingShowRequestDetailResponse;
import com.sharespace.sharespace_server.matching.service.MatchingService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/matching")
public class MatchingController {
	private final MatchingService matchingService;

	@GetMapping()
	public BaseResponse<List<MatchingShowAllResponse>> showAll() {
		Long userId = 2L;
		return matchingService.showAll(userId);
	}

	@PostMapping("/keep")
	public BaseResponse<Void> keep(@Valid @RequestBody MatchingKeepRequest request) {
		return matchingService.keep(request);
	}

	@GetMapping("/keepDetail")
	public BaseResponse<MatchingShowKeepDetailResponse> showKeepDetail(@RequestParam("matchingId") @NotNull Long matchingId) {
		return matchingService.showKeepDetail(matchingId);
	}

	// STORED -> COMPLETED
	@PatchMapping("/completeStorage")
	public BaseResponse<Void> completeStorage(@Valid @RequestBody MatchingCompleteStorageRequest request) {
		Long matchingId = request.getMatchingId();
		return matchingService.completeStorage(matchingId);
	}

	@GetMapping("/requestDetail")
	public BaseResponse<MatchingShowRequestDetailResponse> showRequestDetail(@RequestParam("matchingId") @NotNull Long matchingId) {
		return matchingService.showRequestDetail(matchingId);
	}


	// REQUESTED -> PENDING (호스트가 수락)
	@PostMapping("/acceptRequest/host")
	public BaseResponse<Void> hostAcceptRequest(@Valid @RequestBody MatchingHostAcceptRequestRequest request) {
		return matchingService.hostAcceptRequest(request);
	}

	// PENDING -> STORED
	@PatchMapping("/confirmStorage/guest")
	public BaseResponse<Void> guestConfirmStorage(@Valid @RequestBody MatchingGuestConfirmStorageRequest request) {
		return matchingService.guestConfirmStorage(request);

	}

	@PostMapping("/cancelRequest")
	public BaseResponse<Void> cancelRequest(@RequestParam("matchingId") Long matchingId) {
		return matchingService.cancelRequest(matchingId);
	}

	@PostMapping("/uploadImage/host")
	public BaseResponse<Void> uploadImage(@Valid @RequestBody MatchingUploadImageRequest request) {
		return matchingService.uploadImage(request);
	}
}

