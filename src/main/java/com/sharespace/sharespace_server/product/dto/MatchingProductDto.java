package com.sharespace.sharespace_server.product.dto;

import com.sharespace.sharespace_server.product.entity.Product;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MatchingProductDto {
	private String title;
	private int period;
	private String description;
	private String category;

	public static MatchingProductDto from(Product product) {
		return MatchingProductDto.builder()
			.title(product.getTitle())
			.period(product.getPeriod())
			.description(product.getDescription())
			.category(product.getCategory().toString())
			.build();
	}
}
