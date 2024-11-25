package com.sharespace.sharespace_server.matching.controller;

import static com.sharespace.sharespace_server.global.utils.RequestParser.*;

import com.sharespace.sharespace_server.global.annotation.CheckPermission;
import com.sharespace.sharespace_server.global.enums.Status;
import com.sharespace.sharespace_server.matching.dto.request.MatchingGuestConfirmStorageRequest;
import com.sharespace.sharespace_server.matching.dto.request.MatchingHostAcceptRequestRequest;
import com.sharespace.sharespace_server.matching.dto.response.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.matching.dto.request.MatchingCompleteStorageRequest;
import com.sharespace.sharespace_server.matching.dto.request.MatchingKeepRequest;
import com.sharespace.sharespace_server.matching.dto.request.MatchingUpdateRequest;
import com.sharespace.sharespace_server.matching.dto.request.MatchingUploadImageRequest;
import com.sharespace.sharespace_server.matching.service.MatchingService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matching")
public class MatchingController {
	private final MatchingService matchingService;

	@GetMapping
	@CheckPermission(roles = {"ROLE_GUEST", "ROLE_HOST"})
	public BaseResponse<MatchingShowAllProductWithRoleResponse> showList(
		@RequestParam(value = "status", required = false) Status status,
		HttpServletRequest request) {
		Long userId = extractUserId(request);

		return status == null ?
			matchingService.showAll(userId) : matchingService.showFilteredList(status,userId);
	}

	@PutMapping("/keep")
	@CheckPermission(roles = "ROLE_GUEST")
	public BaseResponse<Void> keep(@Valid @RequestBody MatchingKeepRequest matchingRequest, HttpServletRequest servletRequest) {
		Long userId = extractUserId(servletRequest);
		return matchingService.keep(matchingRequest, userId);
	}

	@GetMapping("/keepDetail")
	@CheckPermission(roles = {"ROLE_GUEST", "ROLE_HOST"})
	public BaseResponse<MatchingShowKeepDetailResponse> showKeepDetail(@RequestParam("matchingId") @NotNull Long matchingId) {
		return matchingService.showKeepDetail(matchingId);
	}

	// STORED -> COMPLETED
	@PatchMapping("/completeStorage")
	@CheckPermission(roles = {"ROLE_GUEST", "ROLE_HOST"})
	public BaseResponse<Void> completeStorage(@Valid @RequestBody MatchingCompleteStorageRequest request, HttpServletRequest servletRequest) {
		Long matchingId = request.getMatchingId();
		Long userId = extractUserId(servletRequest);
		return matchingService.completeStorage(matchingId, userId);
	}

	@GetMapping("/requestDetail")
	@CheckPermission(roles = {"ROLE_GUEST", "ROLE_HOST"})
	public BaseResponse<MatchingShowRequestDetailResponse> showRequestDetail(@RequestParam("matchingId") @NotNull Long matchingId) {
		return matchingService.showRequestDetail(matchingId);
	}


	// REQUESTED -> PENDING (호스트가 수락)
	@PostMapping("/acceptRequest/host")
	@CheckPermission(roles = "ROLE_HOST")
	public BaseResponse<Void> hostAcceptRequest(@Valid @RequestBody MatchingHostAcceptRequestRequest request) {
		return matchingService.hostAcceptRequest(request);
	}

	// PENDING -> STORED
	@PatchMapping("/confirmStorage/guest")
	@CheckPermission(roles = "ROLE_GUEST")
	public BaseResponse<Void> guestConfirmStorage(@Valid @RequestBody MatchingGuestConfirmStorageRequest request) {
		return matchingService.guestConfirmStorage(request);
	}

	// 보관 대기중일 때, Host와 Guest는 '요청 취소'를 할 수 있다.
	@PostMapping("/cancelRequest")
	@CheckPermission(roles = {"ROLE_GUEST", "ROLE_HOST"})
	public BaseResponse<Void> cancelRequest(@RequestParam("matchingId") Long matchingId, HttpServletRequest request) {
		Long userId = extractUserId(request);
		return matchingService.cancelRequest(matchingId, userId);
	}

	@PostMapping("/uploadImage/host")
	@CheckPermission(roles = "ROLE_HOST")
	public BaseResponse<Void> uploadImage(@Valid @ModelAttribute MatchingUploadImageRequest request) {
		return matchingService.uploadImage(request);
	}

	@PatchMapping("/{matchingId}")
	@CheckPermission(roles = "ROLE_GUEST")
	public BaseResponse<Void> updateMatching(@PathVariable Long matchingId,
		@RequestBody MatchingUpdateRequest matchingUpdateRequest,
		HttpServletRequest request
		) {
		Long userId = extractUserId(request);
		return matchingService.updateMatchingWithPlace(matchingId, matchingUpdateRequest, userId);
	}

	@GetMapping("/by-place")
	@CheckPermission(roles = "ROLE_GUEST")
	public BaseResponse<List<MatchingShowAllResponse>> getProductsByPlace(
		@RequestParam Long placeId,
		HttpServletRequest request
	) {
		Long userId = extractUserId(request);
		return matchingService.getProductsByPlace(placeId, userId);
	}

	@GetMapping("/dashboard/count")
	@CheckPermission(roles = "ROLE_HOST")
	public BaseResponse<MatchingDashboardCountResponse> getDashboardCount(HttpServletRequest request) {
		Long userId = extractUserId(request);
		return matchingService.getDashboardCount(userId);
	}

	@GetMapping("/dashboard/upcome")
	@CheckPermission(roles = "ROLE_HOST")
	public BaseResponse<List<MatchingDashboardUpcomeResponse>> getDashboardUpcome(HttpServletRequest request) {
		Long userId = extractUserId(request);
		return matchingService.getDashboardUpcome(userId);
	}

}