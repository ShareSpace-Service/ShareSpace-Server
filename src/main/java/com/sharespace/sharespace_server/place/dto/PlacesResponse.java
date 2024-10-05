package com.sharespace.sharespace_server.place.dto;

import com.sharespace.sharespace_server.global.enums.Category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class PlacesResponse {
	private Long placeId;
	private String title;
	private Category category;
	private String imageUrl;
	private Integer distance;
}
