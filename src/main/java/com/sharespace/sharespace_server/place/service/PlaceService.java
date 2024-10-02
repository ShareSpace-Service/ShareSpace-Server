package com.sharespace.sharespace_server.place.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sharespace.sharespace_server.global.exception.CustomRuntimeException;
import com.sharespace.sharespace_server.global.exception.error.PlaceException;
import com.sharespace.sharespace_server.global.exception.error.UserException;
import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.global.response.BaseResponseService;
import com.sharespace.sharespace_server.place.dto.PlaceDetailResponse;
import com.sharespace.sharespace_server.place.dto.PlaceRequest;
import com.sharespace.sharespace_server.place.dto.PlaceUpdateRequest;
import com.sharespace.sharespace_server.place.dto.PlacesResponse;
import com.sharespace.sharespace_server.place.entity.Place;
import com.sharespace.sharespace_server.place.repository.PlaceRepository;
import com.sharespace.sharespace_server.user.entity.User;
import com.sharespace.sharespace_server.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceService {
	final BaseResponseService baseResponseService;
	private final UserRepository userRepository;
	private final PlaceRepository placeRepository;

	@Transactional
	public BaseResponse<List<PlacesResponse>> getAllPlaces() {
		List<PlacesResponse> placesResponseList =  placeRepository.findAll().stream()
			.map(place -> PlacesResponse.builder()
				.placeId(place.getId())
				.title(place.getTitle())
				.category(place.getCategory())
				.imageUrl(place.getImageUrl())
				// .distance()  // user table에서 가져오기
				.build())
			.collect(Collectors.toList());

		return baseResponseService.getSuccessResponse(placesResponseList);
	}

	@Transactional
	public BaseResponse<List<PlacesResponse>> getLocationOptionsForItem(Long productId) {
		return null;
	}

	@Transactional
	public BaseResponse<PlaceDetailResponse> getPlaceDetail(Long placeId) {
		Place place = placeRepository.findById(placeId).orElseThrow(() -> new CustomRuntimeException(PlaceException.PLACE_NOT_FOUND));

		PlaceDetailResponse placeDetailResponse = PlaceDetailResponse.builder()
			.placeId(placeId)
			.title(place.getTitle())
			.period(place.getPeriod())
			.imageUrl(place.getImageUrl())
			.description(place.getDescription())
			.build();

		return baseResponseService.getSuccessResponse(placeDetailResponse);
	}

	@Transactional
	public BaseResponse<String> createPlace(PlaceRequest placeRequest) {
		User user = userRepository.findById(1L).orElseThrow();
		// request 항목 중 하나라도 빈 값이 들어온 경우 예외 처리
		if(placeRequest.getTitle().isEmpty() || placeRequest.getPeriod() == null || placeRequest.getCategory() == null || placeRequest.getDescription().isEmpty()) {
			throw new CustomRuntimeException(PlaceException.PLACE_REQUIRED_FIELDS_EMPTY);
		}

		Place place = Place.builder()
			.title(placeRequest.getTitle())
			.user(user)
			.period(placeRequest.getPeriod())
			.description(placeRequest.getDescription())
			.category(placeRequest.getCategory())
			.imageUrl(placeRequest.getImage_url())
			.build();

		placeRepository.save(place);

		return baseResponseService.getSuccessResponse("장소가 성공적으로 등록되었습니다.");
	}

	@Transactional
	public BaseResponse<String> updatePlace(PlaceUpdateRequest placeRequest) {
		User user = userRepository.findById(1L)
			.orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));
		user.setLocation(placeRequest.getLocation());

		Place place = placeRepository.findByUserId(user.getId())
			.orElseThrow(() -> new CustomRuntimeException(PlaceException.PLACE_NOT_FOUND));
		place.setTitle(placeRequest.getTitle());
		place.setPeriod(placeRequest.getPeriod());
		place.setCategory(placeRequest.getCategory());
		place.setDescription(placeRequest.getDescription());
		place.setImageUrl(placeRequest.getImage_url());

		userRepository.save(user);
		placeRepository.save(place);

		return baseResponseService.getSuccessResponse("장소 수정 성공!");
	}
}