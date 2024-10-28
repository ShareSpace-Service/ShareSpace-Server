package com.sharespace.sharespace_server.matching.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MatchingShowAllProductWithRoleResponse {
	private String role;
	private List<MatchingShowAllResponse> products;
}
