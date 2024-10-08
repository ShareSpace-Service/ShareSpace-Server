package com.sharespace.sharespace_server.place.dto;

import com.sharespace.sharespace_server.place.entity.Place;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MatchingPlaceDto {
	/*
	* MatchingShowKeepDetailResponse에 사용되는 Dto
	* Composition 사용
	 */

	String title;

	public static MatchingPlaceDto from(Place place) {
		return MatchingPlaceDto.builder()
			.title(place.getTitle())
			.build();
	}
}
