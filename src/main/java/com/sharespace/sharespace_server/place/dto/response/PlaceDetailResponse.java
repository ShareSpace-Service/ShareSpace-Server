package com.sharespace.sharespace_server.place.dto.response;

import java.util.Arrays;
import java.util.List;

import com.sharespace.sharespace_server.place.entity.Place;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class PlaceDetailResponse {
	private Long placeId;
	private String title;
	private String category;
	private Integer period;
	private List<String> imageUrl;
	private String description;

	// from 메서드 추가
	public static PlaceDetailResponse from(Place place) {
		return PlaceDetailResponse.builder()
			.placeId(place.getId())
			.title(place.getTitle())
			.category(place.getCategory().toString()) // 카테고리가 객체라면 toString() 등 적절한 변환 필요
			.period(place.getPeriod())
			.imageUrl(Arrays.asList(place.getImageUrl().split(",")))
			.description(place.getDescription())
			.build();
	}
}
