package com.sharespace.sharespace_server.place.dto;

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
	private String imageUrl;
	private String description;
}
