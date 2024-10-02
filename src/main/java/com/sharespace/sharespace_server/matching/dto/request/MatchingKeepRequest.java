package com.sharespace.sharespace_server.matching.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchingKeepRequest {
	private Long productId;
	private Long placeId;
}