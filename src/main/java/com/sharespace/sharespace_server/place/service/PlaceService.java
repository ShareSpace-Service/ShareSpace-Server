package com.sharespace.sharespace_server.place.service;

import static com.sharespace.sharespace_server.global.utils.LocationTransform.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sharespace.sharespace_server.global.enums.Category;
import com.sharespace.sharespace_server.global.exception.CustomRuntimeException;
import com.sharespace.sharespace_server.global.exception.error.ImageException;
import com.sharespace.sharespace_server.global.exception.error.PlaceException;
import com.sharespace.sharespace_server.global.exception.error.ProductException;
import com.sharespace.sharespace_server.global.exception.error.UserException;
import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.global.response.BaseResponseService;
import com.sharespace.sharespace_server.global.utils.LocationTransform;
import com.sharespace.sharespace_server.global.utils.S3ImageUpload;
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
	private final S3ImageUpload s3ImageUpload;

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
		Place place = placeRepository.findById(placeId)
			.orElseThrow(() -> new CustomRuntimeException(PlaceException.PLACE_NOT_FOUND));

		PlaceDetailResponse placeDetailResponse = PlaceDetailResponse.from(place);

		return baseResponseService.getSuccessResponse(placeDetailResponse);
	}

	@Transactional
	public BaseResponse<String> createPlace(PlaceRequest placeRequest) {
		User user = findByUser(5L);

		// 이미 User Id의 값으로 장소가 만들어져 있는 경우 예외 처리
		placeRepository.findByUserId(user.getId())
			.ifPresent(p -> {
				throw new CustomRuntimeException(PlaceException.PLACE_ALREADY_EXISTS);
			});

		// 이미지 Request 검증
		if (!s3ImageUpload.isRequestImages(placeRequest.getImageUrl())) {
			throw new CustomRuntimeException(ImageException.IMAGE_REQUIRED_FIELDS_EMPTY);
		};

		// 다중 이미지 S3에 업로드
		List<String> combinedImageUrls = s3ImageUpload.uploadMultipleFiles(placeRequest.getImageUrl(), "place/" +user.getId());

		Place place = Place.builder()
			.title(placeRequest.getTitle())
			.user(user)
			.period(placeRequest.getPeriod())
			.description(placeRequest.getDescription())
			.category(placeRequest.getCategory())
			.imageUrl(combinedImageUrls.toString())
			.build();

		placeRepository.save(place);

		return baseResponseService.getSuccessResponse("장소가 성공적으로 등록되었습니다.");
	}

	@Transactional
	public BaseResponse<String> updatePlace(PlaceUpdateRequest placeRequest) {
		User user = findByUser(1L);

		Place place = placeRepository.findByUserId(user.getId())
			.orElseThrow(() -> new CustomRuntimeException(PlaceException.PLACE_NOT_FOUND));

		List<String> updateImages = s3ImageUpload.updateImages(
			placeRequest.getDeleteImageUrl(), placeRequest.getNewImageUrl(),
			"place/" + user.getId(), place.getImageUrl());

		// 이미지 업데이트 처리
		place.setImageUrl(String.join(",", updateImages));

		// 사용자 위치 업데이트
		updateUserLocation(user, placeRequest);
		// 장소 필드 및 이미지 URL 업데이트
		updatePlaceFields(place, placeRequest, updateImages);

		// 변경 사항 저장
		userRepository.save(user);
		placeRepository.save(place);

		return baseResponseService.getSuccessResponse("장소 수정 성공!");
	}

	// task: 장소 정보를 게스트, 호스트 거리를 포함하여 PlacesResponse 객체로 변환
	private PlacesResponse mapToPlacesResponse(Place place, User guest) {
		User host = findByUser(place.getUser().getId());

		Integer distance = calculateDistance(
			guest.getLatitude(), guest.getLongitude(), host.getLatitude(), host.getLongitude()
		);

		return PlacesResponse.builder()
			.placeId(place.getId())
			.title(place.getTitle())
			.category(place.getCategory())
			.imageUrls(List.of(place.getImageUrl().split(","))) // 다중 이미지 URL 배열 설정
			.distance(distance)
			.build();
	}

	// task: user 정보 찾기
	private User findByUser(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));
	}

	// task: 장소 위치 변경 로직
	private void updateUserLocation(User user, PlaceUpdateRequest placeRequest) {
		// 사용자의 위치가 변경되었는지 확인
		if (!user.getLocation().equals(placeRequest.getLocation())) {
			// 새로운 위치에 대한 좌표 변환
			Map<String, Double> coordinates = locationTransform.getCoordinates(placeRequest.getLocation());
			// 사용자 위치 정보 및 좌표 업데이트
			user.setLocation(placeRequest.getLocation());
			user.setLatitude(coordinates.get("latitude"));
			user.setLongitude(coordinates.get("longitude"));
		}
	}

	// task: 장소 필드 업데이트
	private void updatePlaceFields(Place place, PlaceUpdateRequest placeRequest, List<String> updateImages) {
		// 필드별로 null 허용하며, 값이 변경되었을 때만 업데이트
		if (!Objects.equals(place.getTitle(), placeRequest.getTitle())) {
			place.setTitle(placeRequest.getTitle());
		}
		if (!Objects.equals(place.getPeriod(), placeRequest.getPeriod())) {
			place.setPeriod(placeRequest.getPeriod());
		}
		if (!Objects.equals(place.getCategory(), placeRequest.getCategory())) {
			place.setCategory(placeRequest.getCategory());
		}
		if (!Objects.equals(place.getDescription(), placeRequest.getDescription())) {
			place.setDescription(placeRequest.getDescription());
		}

		// 이미지 URL 업데이트
		place.setImageUrl(String.join(",", updateImages));
	}
}
