package com.sharespace.sharespace_server.matching.dto.response;

import com.sharespace.sharespace_server.place.dto.PlaceRequestedDetailResponse;
import com.sharespace.sharespace_server.product.dto.ProductRequestedDetailResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MatchingShowRequestDetailResponse {
	ProductRequestedDetailResponse product;
	PlaceRequestedDetailResponse place;
}
