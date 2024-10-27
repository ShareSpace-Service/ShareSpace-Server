package com.sharespace.sharespace_server.matching.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class MatchingUpdateRequest {
	@NotNull
	private Long placeId;
}
