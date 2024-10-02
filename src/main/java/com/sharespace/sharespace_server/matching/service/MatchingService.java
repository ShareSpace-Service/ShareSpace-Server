package com.sharespace.sharespace_server.matching.service;

import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import com.sharespace.sharespace_server.global.enums.Status;
import com.sharespace.sharespace_server.global.exception.CustomException;
import com.sharespace.sharespace_server.global.exception.CustomRuntimeException;
import com.sharespace.sharespace_server.global.exception.error.PlaceException;
import com.sharespace.sharespace_server.global.exception.error.ProductException;
import com.sharespace.sharespace_server.matching.dto.request.MatchingRequestDto;
import com.sharespace.sharespace_server.matching.entity.Matching;
import com.sharespace.sharespace_server.matching.repository.MatchingRepository;
import com.sharespace.sharespace_server.place.entity.Place;
import com.sharespace.sharespace_server.place.repository.PlaceRepository;
import com.sharespace.sharespace_server.product.entity.Product;
import com.sharespace.sharespace_server.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchingService {
	private final MatchingRepository matchingRepository;
	private final ProductRepository productRepository;
	private final PlaceRepository placeRepository;

	public void keep(MatchingRequestDto matchingRequestDto) {
		// Place와 Product를 찾고, 유효성 검사를 함
		Place place = placeRepository.findById(matchingRequestDto.getPlaceId())
		.orElseThrow(() -> new CustomRuntimeException(PlaceException.PLACE_NOT_FOUND));
		Product product = productRepository.findById(matchingRequestDto.getProductId())
			.orElseThrow(() -> new CustomRuntimeException(ProductException.PRODUCT_NOT_FOUND));
		// Matching 엔티티 생성 후 필요한 값 설정
		Matching matching = new Matching();
		matching.setProduct(product);
		matching.setPlace(place);
		matching.setStatus(Status.REQUESTED);
		// TODO : Distance 계산하여 컬럼에 추가해야함

		matchingRepository.save(matching);
	}
}
