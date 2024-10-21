package com.sharespace.sharespace_server.matching.dto.request;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchingUploadImageRequest {
	@NotNull(message = "MatchingId는 NULL이여서는 안 됩니다.")
	private Long matchingId;

	@NotNull(message = "이미지 URL은 NULL이여서는 안 됩니다.")
	private MultipartFile imageUrl;
}
