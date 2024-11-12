package com.sharespace.sharespace_server.place.dto;

import java.util.Arrays;
import java.util.List;

import com.sharespace.sharespace_server.place.entity.Place;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PlaceEditResponse {
	private Long placeId;
	private String location;
	private String title;
	private String category;
	private Integer period;
	private List<String> imageUrl;
	private String description;

	public static PlaceEditResponse of(Place place, String location) {
		return PlaceEditResponse.builder()
			.placeId(place.getId())
			.location(location)
			.title(place.getTitle())
			.category(place.getCategory().toString())
			.period(place.getPeriod())
			.imageUrl(Arrays.asList(place.getImageUrl().split(",")))
			.description(place.getDescription())
			.build();
	}
}
