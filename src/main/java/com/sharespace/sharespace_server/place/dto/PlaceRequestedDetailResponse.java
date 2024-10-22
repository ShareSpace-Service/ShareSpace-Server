package com.sharespace.sharespace_server.place.dto;

import java.util.List;

import com.sharespace.sharespace_server.global.enums.Category;
import com.sharespace.sharespace_server.place.entity.Place;

import lombok.Builder;
import lombok.Getter;

/*
 * MatchingController에서 사용되는 DTO
 * /matching/requestDetail?matchingId={}로 들어온 요청 처리
 * MatchingShowRequestDetailResponse에서 Composition 사용
 */
@Getter
@Builder
public class PlaceRequestedDetailResponse {
	private String title;
	private List<String> image;
	private Category category;
	private int period;
	private String description;

	public static PlaceRequestedDetailResponse of(Place place) {
		return PlaceRequestedDetailResponse.builder()
			.title(place.getTitle())
			.image(List.of(place.getImageUrl().split(",")))
			.category(place.getCategory())
			.period(place.getPeriod())
			.description(place.getDescription())
			.build();
	}
}
