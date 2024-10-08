package com.sharespace.sharespace_server.place.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

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
	private List<MultipartFile> imageUrl;
	private String description;
}
