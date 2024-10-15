package com.sharespace.sharespace_server.matching.dto;

import static com.sharespace.sharespace_server.global.enums.Status.*;

import com.sharespace.sharespace_server.matching.dto.response.MatchingShowAllResponse;
import com.sharespace.sharespace_server.matching.entity.Matching;
import com.sharespace.sharespace_server.matching.repository.MatchingRepository;
import com.sharespace.sharespace_server.product.entity.Product;
import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class MatchingAssembler {

	private final MatchingRepository matchingRepository;

	public MatchingShowAllResponse toMatchingShowAllResponse(Product product) {
		List<String> imageUrls = List.of(product.getImageUrl().split(","));

		if (!product.getIsPlaced()) {
			return MatchingShowAllResponse.builder()
				.matchingId(null)
				.title(product.getTitle())
				.category(product.getCategory())
				.imageUrl(imageUrls)
				.status(UNASSIGNED)
				.distance(null)
				.build();
		} else {
			Matching matching = matchingRepository.findByProductId(product.getId());
			return MatchingShowAllResponse.builder()
				.matchingId(matching.getId())
				.title(product.getTitle())
				.category(product.getCategory())
				.imageUrl(imageUrls)
				.status(matching.getStatus())
				.distance(matching.getDistance())
				.build();
		}
	}
}
