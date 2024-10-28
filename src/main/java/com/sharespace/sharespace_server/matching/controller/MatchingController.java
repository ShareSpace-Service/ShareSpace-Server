package com.sharespace.sharespace_server.matching.controller;

import static com.sharespace.sharespace_server.global.utils.RequestParser.*;

import com.sharespace.sharespace_server.global.enums.Status;
import com.sharespace.sharespace_server.global.exception.CustomRuntimeException;
import com.sharespace.sharespace_server.global.exception.error.UserException;
import com.sharespace.sharespace_server.matching.dto.request.MatchingGuestConfirmStorageRequest;
import com.sharespace.sharespace_server.matching.dto.request.MatchingHostAcceptRequestRequest;
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
import com.sharespace.sharespace_server.matching.dto.response.MatchingShowAllProductWithRoleResponse;
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

	@GetMapping
	public BaseResponse<MatchingShowAllProductWithRoleResponse> showList(
		@RequestParam(value = "status", required = false) Status status,
		HttpServletRequest request) {
		Long userId = extractUserId(request);

		if (status == null) {
			return matchingService.showAll(userId);
		} else {
			return matchingService.showFilteredList(status, userId);
		}
	}

	@PutMapping("/keep")
	public BaseResponse<Void> keep(@Valid @RequestBody MatchingKeepRequest matchingRequest, HttpServletRequest servletRequest) {
		String currentUserRole = getCurrentUserRole();
		// TODO : Spring AOP 사용하여 권한 관련 로직 중앙화하기
		if (!currentUserRole.equals("ROLE_GUEST")) {
			throw new CustomRuntimeException(UserException.NOT_AUTHORIZED);
		}
		Long userId = extractUserId(servletRequest);
		return matchingService.keep(matchingRequest, userId);
	}

	@GetMapping("/keepDetail")
	public BaseResponse<MatchingShowKeepDetailResponse> showKeepDetail(@RequestParam("matchingId") @NotNull Long matchingId) {
		return matchingService.showKeepDetail(matchingId);
	}

	// STORED -> COMPLETED
	@PatchMapping("/completeStorage")
	public BaseResponse<Void> completeStorage(@Valid @RequestBody MatchingCompleteStorageRequest request, HttpServletRequest servletRequest) {
		Long matchingId = request.getMatchingId();
		Long userId = extractUserId(servletRequest);
		return matchingService.completeStorage(matchingId, userId);
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

	// 보관 대기중일 때, Host와 Guest는 '요청 취소'를 할 수 있다.
	@PostMapping("/cancelRequest")
	public BaseResponse<Void> cancelRequest(@RequestParam("matchingId") Long matchingId, HttpServletRequest request) {
		Long userId = extractUserId(request);
		return matchingService.cancelRequest(matchingId, userId);
	}

	@PostMapping("/uploadImage/host")
	public BaseResponse<Void> uploadImage(@Valid @ModelAttribute MatchingUploadImageRequest request) {
		return matchingService.uploadImage(request);
	}

	@PatchMapping("/{matchingId}")
	public BaseResponse<Void> updateMatching(@PathVariable Long matchingId,
		@RequestBody MatchingUpdateRequest matchingUpdateRequest,
		HttpServletRequest request
		) {
		Long userId = extractUserId(request);
		return matchingService.updateMatchingWithPlace(matchingId, matchingUpdateRequest, userId);
	}

	@GetMapping("/by-place")
	public BaseResponse<List<MatchingShowAllResponse>> getProductsByPlace(
		@RequestParam Long placeId,
		HttpServletRequest request
	) {
		Long userId = extractUserId(request);
		return matchingService.getProductsByPlace(placeId, userId);
	}


}

