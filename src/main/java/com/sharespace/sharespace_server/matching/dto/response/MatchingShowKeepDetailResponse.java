package com.sharespace.sharespace_server.matching.dto.response;

import com.sharespace.sharespace_server.place.dto.PlaceDto;
import com.sharespace.sharespace_server.product.dto.ProductDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MatchingShowKeepDetailResponse {
	private ProductDto product;
	private PlaceDto place;
	private String imageUrl;
}
