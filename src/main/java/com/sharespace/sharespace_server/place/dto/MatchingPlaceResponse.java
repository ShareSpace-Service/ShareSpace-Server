package com.sharespace.sharespace_server.place.dto;

import com.sharespace.sharespace_server.place.entity.Place;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MatchingPlaceResponse {
	/*
	* MatchingShowKeepDetailResponse에 사용되는 Dto
	* Composition 사용
	 */

	String title;

	public static MatchingPlaceResponse from(Place place) {
		return MatchingPlaceResponse.builder()
			.title(place.getTitle())
			.build();
	}
}
