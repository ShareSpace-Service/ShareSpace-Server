package com.sharespace.sharespace_server.matching.dto;


import com.sharespace.sharespace_server.matching.dto.response.MatchingDashboardUpcomeResponse;
import com.sharespace.sharespace_server.matching.dto.response.MatchingShowAllResponse;
import com.sharespace.sharespace_server.matching.entity.Matching;
import com.sharespace.sharespace_server.product.entity.Product;
import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class MatchingAssembler {


	public MatchingShowAllResponse toMatchingShowAllResponse(Matching matching) {
		Product product = matching.getProduct();
		List<String> imageUrls = List.of(matching.getProduct().getImageUrl().split(","));
		return MatchingShowAllResponse.builder()
			.matchingId(matching.getId())
			.title(product.getTitle())
			.category(product.getCategory())
			.imageUrl(imageUrls)
			.status(matching.getStatus())
			.distance(matching.getDistance())
			.build();
	}

	public MatchingDashboardUpcomeResponse toMatchingShowDashboard(Matching matching, int remainingDays) {
		Product product = matching.getProduct();
		List<String> imageUrls = List.of(matching.getProduct().getImageUrl().split(","));
		return MatchingDashboardUpcomeResponse.builder()
				.matchingId(matching.getId())
				.title(product.getTitle())
				.category(product.getCategory())
				.imageUrl(imageUrls)
				.status(matching.getStatus())
				.distance(matching.getDistance())
				.remainingDays(remainingDays)
				.build();
	}
}
