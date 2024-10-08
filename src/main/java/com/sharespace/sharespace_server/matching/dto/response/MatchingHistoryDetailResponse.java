package com.sharespace.sharespace_server.matching.dto.response;

import com.sharespace.sharespace_server.place.dto.MatchingPlaceDto;
import com.sharespace.sharespace_server.product.dto.MatchingProductDto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MatchingHistoryDetailResponse {
	private MatchingProductDto matchingProductDto;
	private MatchingPlaceDto matchingPlaceDto;
	private String imageUrl;
}
