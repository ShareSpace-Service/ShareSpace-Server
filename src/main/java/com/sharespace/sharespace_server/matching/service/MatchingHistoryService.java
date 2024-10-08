package com.sharespace.sharespace_server.matching.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sharespace.sharespace_server.global.enums.Status;
import com.sharespace.sharespace_server.global.exception.CustomRuntimeException;
import com.sharespace.sharespace_server.global.exception.error.MatchingException;
import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.global.response.BaseResponseService;
import com.sharespace.sharespace_server.matching.dto.response.MatchingHistoryResponse;
import com.sharespace.sharespace_server.matching.dto.response.MatchingShowKeepDetailResponse;
import com.sharespace.sharespace_server.matching.entity.Matching;
import com.sharespace.sharespace_server.matching.repository.MatchingRepository;
import com.sharespace.sharespace_server.place.dto.MatchingPlaceDto;
import com.sharespace.sharespace_server.product.dto.MatchingProductDto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Service
public class MatchingHistoryService {
	private final BaseResponseService baseResponseService;
	private final MatchingRepository matchingRepository;
	public BaseResponse<List<MatchingHistoryResponse>> getHistory() {
		List<MatchingHistoryResponse> responses = matchingRepository.findAllByStatus(Status.COMPLETED).stream()
			.map(matching -> MatchingHistoryResponse.builder()
				.matchingId(matching.getId())
				.title(matching.getProduct().getTitle())
				.category(matching.getProduct().getCategory())
				.imageUrl(matching.getImage())
				.distance(matching.getDistance())
				.build())
			.toList();
		return baseResponseService.getSuccessResponse(responses);
	}

	public BaseResponse<MatchingShowKeepDetailResponse> getHistoryDetail(Long matchingId) {
		Matching matching = matchingRepository.findById(matchingId)
			.orElseThrow(() -> new CustomRuntimeException(MatchingException.MATCHING_NOT_FOUND));

		MatchingProductDto matchingProductDto = MatchingProductDto.from(matching.getProduct());
		MatchingPlaceDto matchingPlaceDto = MatchingPlaceDto.from(matching.getPlace());

		MatchingShowKeepDetailResponse response = MatchingShowKeepDetailResponse.builder()
			.product(matchingProductDto)
			.place(matchingPlaceDto)
			.imageUrl(matching.getImage())
			.build();

		return baseResponseService.getSuccessResponse(response);
	}
}
