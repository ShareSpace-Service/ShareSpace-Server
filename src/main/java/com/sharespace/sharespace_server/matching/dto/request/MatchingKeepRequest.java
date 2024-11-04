package com.sharespace.sharespace_server.matching.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchingKeepRequest {
	@NotNull
	private Long matchingId;
	@NotNull
	private Long placeId;
}