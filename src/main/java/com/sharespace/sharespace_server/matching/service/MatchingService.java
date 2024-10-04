package com.sharespace.sharespace_server.matching.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.sharespace.sharespace_server.global.enums.Status;
import com.sharespace.sharespace_server.global.exception.CustomRuntimeException;
import com.sharespace.sharespace_server.global.exception.error.MatchingException;
import com.sharespace.sharespace_server.global.exception.error.PlaceException;
import com.sharespace.sharespace_server.global.exception.error.ProductException;
import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.global.response.BaseResponseService;
import com.sharespace.sharespace_server.matching.dto.request.MatchingKeepRequest;
import com.sharespace.sharespace_server.matching.dto.response.MatchingShowKeepDetailResponse;
import com.sharespace.sharespace_server.matching.entity.Matching;
import com.sharespace.sharespace_server.matching.repository.MatchingRepository;
import com.sharespace.sharespace_server.place.dto.PlaceDto;
import com.sharespace.sharespace_server.place.entity.Place;
import com.sharespace.sharespace_server.place.repository.PlaceRepository;
import com.sharespace.sharespace_server.product.dto.ProductDto;
import com.sharespace.sharespace_server.product.entity.Product;
import com.sharespace.sharespace_server.product.repository.ProductRepository;

import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchingService {

	private final BaseResponseService baseResponseService;
	private final MatchingRepository matchingRepository;
	private final ProductRepository productRepository;
	private final PlaceRepository placeRepository;

	public BaseResponse<Void> keep(MatchingKeepRequest matchingKeepRequest) {
		// Place와 Product를 찾고, 유효성 검사
		Place place = placeRepository.findById(matchingKeepRequest.getPlaceId())
		.orElseThrow(() -> new CustomRuntimeException(PlaceException.PLACE_NOT_FOUND));
		Product product = productRepository.findById(matchingKeepRequest.getProductId())
			.orElseThrow(() -> new CustomRuntimeException(ProductException.PRODUCT_NOT_FOUND));

		// 2024-10-04 : Product의 카테고리 값이 Place보다 클 경우 예외처리
		if (place.getCategory().getValue() < product.getCategory().getValue()) {
			throw new CustomRuntimeException(MatchingException.CATEGORY_NOT_MATCHED);
		}

		// 2024-10-04 : 이미 존재하는 Matching의 경우 예외처리
		if (matchingRepository.findByProductIdAndPlaceId(product.getId(), place.getId()).isPresent()) {
			throw new CustomRuntimeException(MatchingException.ALREADY_EXISTED_MATCHING);
		}

		// Matching 엔티티 생성 후 필요한 값 설정
		Matching matching = new Matching();
		matching.setProduct(product);
		matching.setPlace(place);
		matching.setStatus(Status.REQUESTED);
		matching.setStartDate(LocalDateTime.now());
		matching.setDistance(2); //
		// TODO : Product와 Place의 Distance 계산하여 컬럼에 추가해야함


		// 2024-10-04 : Place의 isPlaced 컬럼을 true로 업데이트
		product.setIsPlaced(true);
		productRepository.save(product);


		matchingRepository.save(matching);
		return baseResponseService.getSuccessResponse();
	}

	public BaseResponse<MatchingShowKeepDetailResponse> showKeepDetail(Long matchingId) {
		Matching matching = matchingRepository.findById(matchingId)
			.orElseThrow(() -> new CustomRuntimeException(MatchingException.CANNOT_FIND_MATCHING));

		ProductDto productDto = ProductDto.from(matching.getProduct());
		PlaceDto placeDto = PlaceDto.from(matching.getPlace());

		MatchingShowKeepDetailResponse response = MatchingShowKeepDetailResponse.builder()
			.product(productDto)
			.place(placeDto)
			.imageUrl(matching.getImage())
			.build();

		return baseResponseService.getSuccessResponse(response);
	}
}
