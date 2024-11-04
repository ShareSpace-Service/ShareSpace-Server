package com.sharespace.sharespace_server.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ProductRegisterResponse {
	// 2024-10-27 matchingId 추가
	private Long matchingId;
}
