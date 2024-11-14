package com.sharespace.sharespace_server.place.dto.request;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.sharespace.sharespace_server.global.enums.Category;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@RequiredArgsConstructor
public class PlaceRequest {
	@NotNull(message = "Title을 입력해주세요")
	private String title;
	@NotNull(message = "Category를 선택해주세요")
	private Category category;
	@NotNull(message = "최대 보관 기간을 입력해주세요")
	private Integer period;
	@NotNull(message = "이미지를 선택해주세요")
	private List<MultipartFile> imageUrl;
	private String description;
}
