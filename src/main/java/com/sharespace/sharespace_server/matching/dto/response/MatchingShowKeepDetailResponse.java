package com.sharespace.sharespace_server.matching.dto.response;

import com.sharespace.sharespace_server.place.dto.MatchingPlaceResponse;
import com.sharespace.sharespace_server.product.dto.MatchingProductDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MatchingShowKeepDetailResponse {
	private MatchingProductDto product;
	private MatchingPlaceResponse place;
	private String imageUrl;
}
