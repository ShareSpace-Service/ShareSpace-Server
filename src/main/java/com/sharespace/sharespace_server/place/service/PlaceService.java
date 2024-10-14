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

	/**
	 * <p>모든 장소 리스트를 불러와 게스트 사용자 정보를 포함한 PlacesResponse 리스트로 반환합니다.</p>
	 *
	 * <p>이 메서드는 데이터베이스에서 모든 장소 정보를 조회하고, 각 장소에 대해 게스트 사용자와 호스트 간의 거리를 계산하여
	 * PlacesResponse 객체로 변환합니다.</p>
	 *
	 * @return 모든 장소 정보를 담은 PlacesResponse 리스트를 성공 응답 형태로 반환합니다.
	 * @Author thereisname
	 */
	@Transactional
	public BaseResponse<List<PlacesResponse>> getAllPlaces() {
		User guest = findByUser(2L);

		List<PlacesResponse> placesResponseList = placeRepository.findAll().stream()
			.map(place -> mapToPlacesResponse(place, guest))
			.collect(Collectors.toList());

		return baseResponseService.getSuccessResponse(placesResponseList);
	}

	/**
	 * <p>주어진 상품의 카테고리와 관련된 장소 리스트를 조회하여 PlacesResponse 리스트로 반환합니다.</p>
	 *
	 * <p>이 메서드는 주어진 상품 ID를 통해 상품을 조회한 후, 해당 상품의 카테고리와 그와 연관된 카테고리
	 * (예: 상품이 "Medium"인 경우 "Medium"과 "Large" 카테고리)를 기반으로 장소를 조회하고, 이를 게스트 사용자 정보를
	 * 포함한 PlacesResponse 리스트로 변환하여 반환합니다.</p>
	 *
	 * @param productId 상품 ID (Long 타입)
	 * @return 상품 카테고리에 맞는 장소 리스트를 담은 PlacesResponse 객체 리스트를 성공 응답 형태로 반환합니다.
	 * @Author thereisname
	 */
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

	/**
	 * <p>주어진 장소 ID를 통해 장소 상세 정보를 조회하여 PlaceDetailResponse로 반환합니다.</p>
	 *
	 * <p>이 메서드는 주어진 장소 ID로 장소를 조회한 후, 조회된 장소 정보를 PlaceDetailResponse 객체로 변환하여 반환합니다.
	 * 장소가 존재하지 않을 경우 예외를 발생시킵니다.</p>
	 *
	 * @param placeId 장소 ID (Long 타입)
	 * @return 조회된 장소 정보를 담은 PlaceDetailResponse 객체를 성공 응답 형태로 반환합니다.
	 * @Author thereisname
	 */
	@Transactional
	public BaseResponse<PlaceDetailResponse> getPlaceDetail(Long placeId) {
		Place place = placeRepository.findById(placeId)
			.orElseThrow(() -> new CustomRuntimeException(PlaceException.PLACE_NOT_FOUND));

		PlaceDetailResponse placeDetailResponse = PlaceDetailResponse.from(place);

		return baseResponseService.getSuccessResponse(placeDetailResponse);
	}

	/**
	 * <p>주어진 요청 데이터를 사용하여 새로운 장소를 생성하고 성공 메시지를 반환합니다.</p>
	 *
	 * <p>이 메서드는 사용자 ID를 기반으로 장소를 생성하며, 이미 동일한 사용자 ID로 등록된 장소가 있을 경우
	 * 예외를 발생시킵니다. 또한, 이미지 업로드를 처리하며, 이미지가 없거나 유효하지 않을 경우에도 예외를 발생시킵니다.</p>
	 *
	 * @param placeRequest 장소 생성에 필요한 요청 데이터 (PlaceRequest 타입)
	 * @return 성공적으로 생성된 장소에 대한 성공 메시지를 반환합니다.
	 * @throws CustomRuntimeException 동일한 사용자 ID로 이미 장소가 등록경우, 이미지가 유효하지 않거나 누락된 경우 발생합니다.
	 * @Author thereisname
	 */
	@Transactional
	public BaseResponse<String> createPlace(PlaceRequest placeRequest) {
		User user = findByUser(5L);

		// 이미 User Id의 값으로 장소가 만들어져 있는 경우 예외 처리
		placeRepository.findByUserId(user.getId())
			.ifPresent(p -> {
				throw new CustomRuntimeException(PlaceException.PLACE_ALREADY_EXISTS);
			});

		// 이미지 Request 검증
		if (!s3ImageUpload.hasValidImages(placeRequest.getImageUrl())) {
			throw new CustomRuntimeException(ImageException.IMAGE_REQUIRED_FIELDS_EMPTY);
		};

		// 다중 이미지 S3에 업로드
		List<String> combinedImageUrls = s3ImageUpload.uploadImages(placeRequest.getImageUrl(), "place/" +user.getId());

		Place place = Place.builder()
			.title(placeRequest.getTitle())
			.user(user)
			.period(placeRequest.getPeriod())
			.description(placeRequest.getDescription())
			.category(placeRequest.getCategory())
			.imageUrl(String.join(",", combinedImageUrls))
			.build();

		placeRepository.save(place);

		return baseResponseService.getSuccessResponse("장소가 성공적으로 등록되었습니다.");
	}

	/**
	 * <p>주어진 요청 데이터를 사용하여 기존 장소 정보를 업데이트하고 성공 메시지를 반환합니다.</p>
	 *
	 * <p>이 메서드는 사용자 ID를 기반으로 해당 사용자가 소유한 장소를 조회한 후, 이미지, 위치, 기타 필드들을
	 * 업데이트합니다. 특히, 이미지 세트를 업데이트하고, 사용자의 위치 정보가 변경된 경우 그에 따라 사용자 좌표를
	 * 갱신하며, 장소의 기타 필드들도 요청된 값으로 변경됩니다.</p>
	 *
	 * @param placeRequest 장소 업데이트에 필요한 요청 데이터 (PlaceUpdateRequest 타입)
	 * @return 장소 수정에 대한 성공 메시지를 반환합니다.
	 *
	 * @throws CustomRuntimeException  해당 사용자 ID로 등록된 장소가 존재하지 않는 경우 PLACE_NOT_FOUND 예외가 발생합니다.
	 *
	 * @Author thereisname
	 */
	@Transactional
	public BaseResponse<String> updatePlace(PlaceUpdateRequest placeRequest) {
		User user = findByUser(1L);

		Place place = placeRepository.findByUserId(user.getId())
			.orElseThrow(() -> new CustomRuntimeException(PlaceException.PLACE_NOT_FOUND));

		List<String> updateImages = s3ImageUpload.updateImageSet(
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
