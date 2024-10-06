package com.sharespace.sharespace_server.product.dto;

import com.sharespace.sharespace_server.global.enums.Category;
import com.sharespace.sharespace_server.product.entity.Product;

import lombok.Builder;
import lombok.Getter;

/*
 * MatchingController에서 사용되는 DTO
 * /matching/requestDetail?matchingId={}로 들어온 요청 처리
 * MatchingShowRequestDetailResponse에서 Composition 사용
 */
@Getter
@Builder
public class ProductRequestedDetailResponse {
	private String title;
	private String image;
	private Category category;
	private int period;
	private String description;

	public static ProductRequestedDetailResponse of(
	Product product) {
		return ProductRequestedDetailResponse.builder()
			.title(product.getTitle())
			.image(product.getImageUrl())
			.category(product.getCategory())
			.period(product.getPeriod())
			.description(product.getDescription())
			.build();
	}
}
