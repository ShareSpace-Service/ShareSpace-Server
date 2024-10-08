package com.sharespace.sharespace_server.matching.dto.response;

import com.sharespace.sharespace_server.global.enums.Category;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class MatchingHistoryResponse {
	private Long matchingId;
	private String title;
	private Category category;
	private String imageUrl;
	private Integer distance;
}
