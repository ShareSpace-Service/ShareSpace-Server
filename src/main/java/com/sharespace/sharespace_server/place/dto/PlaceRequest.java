package com.sharespace.sharespace_server.place.dto;

import com.sharespace.sharespace_server.global.enums.Category;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@RequiredArgsConstructor
public class PlaceRequest {
	private String title;
	private Category category;
	private Integer period;
	private String image_url;
	private String description;
}
