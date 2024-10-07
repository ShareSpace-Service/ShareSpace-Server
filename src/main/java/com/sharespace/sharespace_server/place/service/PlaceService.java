package com.sharespace.sharespace_server.place.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import com.sharespace.sharespace_server.global.enums.Category;
import com.sharespace.sharespace_server.global.exception.CustomRuntimeException;
import com.sharespace.sharespace_server.global.exception.error.PlaceException;
import com.sharespace.sharespace_server.global.exception.error.ProductException;
import com.sharespace.sharespace_server.global.exception.error.UserException;
import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.global.response.BaseResponseService;
import com.sharespace.sharespace_server.global.utils.LocationTransform;
import com.sharespace.sharespace_server.place.dto.PlaceDetailResponse;
import com.sharespace.sharespace_server.place.dto.PlaceRequest;
import com.sharespace.sharespace_server.place.dto.PlaceUpdateRequest;
import com.sharespace.sharespace_server.place.dto.PlacesResponse;
import com.sharespace.sharespace_server.place.entity.Place;
import com.sharespace.sharespace_server.place.repository.PlaceRepository;
import com.sharespace.sharespace_server.product.entity.Product;
import com.sharespace.sharespace_server.product.repository.ProductRepository;
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
	private final ProductRepository productRepository;
	private final LocationTransform locationTransform;
	private final Bucket bucket;

	/**
	 * 주어진 장소 정보를 게스트 사용자와 함께 PlacesResponse 객체로 매핑합니다.
	 *
	 * @param place 장소 정보
	 * @param guest 게스트 사용자
	 * @return 매핑된 PlacesResponse 객체
	 * @throws CustomRuntimeException 호스트 사용자가 존재하지 않을 경우 발생
	 */
	private PlacesResponse mapToPlacesResponse(Place place, User guest) {
		User host = userRepository.findById(place.getUser().getId())
			.orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));

		Integer distance = locationTransform.calculateDistance(
			guest.getLatitude(), guest.getLongitude(), host.getLatitude(), host.getLongitude()
		);

		return PlacesResponse.builder()
			.placeId(place.getId())
			.title(place.getTitle())
			.category(place.getCategory())
			.imageUrl(place.getImageUrl())
			.distance(distance)
			.build();
	}

	// user 정보 찾기
	private User findByUser(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));
	}

	// 파일 다운로드 메서드 (다운로드 링크 반환)
	public String getFileUrl(String fileName) {
		String url = bucket.get("download.jpeg").signUrl(1, TimeUnit.HOURS).toString();

		return url;
	}

	@Transactional
	public BaseResponse<List<PlacesResponse>> getAllPlaces() {
		User guest = findByUser(2L);

		List<PlacesResponse> placesResponseList = placeRepository.findAll().stream()
			.map(place -> mapToPlacesResponse(place, guest))
			.collect(Collectors.toList());

		return baseResponseService.getSuccessResponse(placesResponseList);
	}

	@Transactional
	public BaseResponse<List<PlacesResponse>> getLocationOptionsForItem(Long productId) {
		User guest = findByUser(2L);

		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new CustomRuntimeException(ProductException.PRODUCT_NOT_FOUND));

		List<Category> categories = product.getCategory().getRelatedCategories();

		List<PlacesResponse> places = placeRepository.findAllByCategoryIn(categories).stream()
			.map(place -> mapToPlacesResponse(place, guest))
			.toList();

		return baseResponseService.getSuccessResponse(places);
	}

	@Transactional
	public BaseResponse<PlaceDetailResponse> getPlaceDetail(Long placeId) {
		Place place = placeRepository.findById(placeId).orElseThrow(() -> new CustomRuntimeException(PlaceException.PLACE_NOT_FOUND));

		PlaceDetailResponse placeDetailResponse = PlaceDetailResponse.from(place);

		return baseResponseService.getSuccessResponse(placeDetailResponse);
	}

	@Transactional
	public BaseResponse<String> createPlace(PlaceRequest placeRequest) {
		User user = findByUser(1L);

		// request 항목 중 하나라도 빈 값이 들어온 경우 예외 처리
		if(placeRequest.getTitle().isEmpty() || placeRequest.getPeriod() == null
			|| placeRequest.getCategory() == null || placeRequest.getDescription().isEmpty()) {
			throw new CustomRuntimeException(PlaceException.PLACE_REQUIRED_FIELDS_EMPTY);
		}

		// 이미 User Id의 값으로 장소가 만들어져 있는 경우 예외 처리
		placeRepository.findByUserId(user.getId())
			.ifPresent(p -> {
				throw new CustomRuntimeException(PlaceException.PLACE_ALREADY_EXISTS);
			});

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
		// request 항목 중 하나라도 빈 값이 들어온 경우 예외 처리
		if(placeRequest.getTitle().isEmpty() || placeRequest.getPeriod() == null
			|| placeRequest.getCategory() == null || placeRequest.getDescription().isEmpty()
			|| placeRequest.getLocation().isEmpty()) {
			throw new CustomRuntimeException(PlaceException.PLACE_REQUIRED_FIELDS_EMPTY);
		}

		User user = findByUser(1L);

		Map<String, Double> coordinates = locationTransform.getCoordinates(placeRequest.getLocation());

		user.setLocation(placeRequest.getLocation());
		user.setLatitude(coordinates.get("latitude"));
		user.setLongitude(coordinates.get("longitude"));

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
