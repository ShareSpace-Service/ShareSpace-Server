package com.sharespace.sharespace_server.matching.dto.response;

import com.sharespace.sharespace_server.global.enums.Category;
import com.sharespace.sharespace_server.global.enums.Status;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MatchingShowAllResponse {
	private Long matchingId;
	private String title;
	private Category category;
	private List<String> imageUrl; // product 이미지
	private Status status;
	private Integer distance;
	private String role;
}
