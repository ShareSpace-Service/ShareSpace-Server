package com.sharespace.sharespace_server.place.dto;

import com.sharespace.sharespace_server.place.entity.Place;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PlaceDto {
	/*
	* MatchingShowKeepDetailResponse에 사용되는 Dto
	* Composition 사용
	 */

	String title;

	public static PlaceDto from(Place place) {
		return PlaceDto.builder()
			.title(place.getTitle())
			.build();
	}
}
