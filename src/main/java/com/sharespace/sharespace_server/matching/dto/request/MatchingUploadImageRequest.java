package com.sharespace.sharespace_server.matching.dto.request;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class MatchingUploadImageRequest {
	@NotNull
	private Long matchingId;

	@NotNull
	private List<MultipartFile> imageUrl;
}
