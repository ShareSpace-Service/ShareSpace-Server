package com.sharespace.sharespace_server.place.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sharespace.sharespace_server.global.exception.CustomRuntimeException;
import com.sharespace.sharespace_server.global.exception.error.ImageException;
import com.sharespace.sharespace_server.global.exception.error.MatchingException;
import com.sharespace.sharespace_server.global.exception.error.PlaceException;
import com.sharespace.sharespace_server.global.exception.error.ProductException;
import com.sharespace.sharespace_server.global.exception.error.UserException;
import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.global.response.BaseResponseService;
import com.sharespace.sharespace_server.global.utils.LocationTransform;
import com.sharespace.sharespace_server.global.utils.S3ImageUpload;
import com.sharespace.sharespace_server.matching.entity.Matching;
import com.sharespace.sharespace_server.matching.repository.MatchingRepository;
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
	private final MatchingRepository matchingRepository;

	/**
	 * <p>모든 장소 리스트를 불러와 게스트 사용자 정보를 포함한 PlacesResponse 리스트로 반환합니다.</p>
	 *
	 * <p>이 메서드는 데이터베이스에서 모든 장소 정보를 조회하고, 각 장소에 대해 게스트 사용자와 호스트 간의 거리를 계산하여
	 * PlacesResponse 객체로 변환합니다.</p>
	 *
	 * @param userId 현재 로그인한 사용자 Id
	 * @return 모든 장소 정보를 담은 PlacesResponse 리스트를 성공 응답 형태로 반환합니다.
	 * @Author thereisname
	 */
	@Transactional
	public BaseResponse<List<PlacesResponse>> getAllPlaces(Long userId) {
		User guest = findByUser(userId);

		List<PlacesResponse> placesResponseList = placeRepository.findAll().stream()
			.map(place -> PlacesResponse.from(place, guest))
			.collect(Collectors.toList());

		return baseResponseService.getSuccessResponse(placesResponseList);
	}

	/**
	 * <p>주어진 매칭 ID에 해당하는 상품의 카테고리에 맞는 장소 리스트를 조회하여 PlacesResponse 리스트 형태로 반환</p>
	 *
	 * <p>이 메서드는 주어진 매칭 ID를 통해 해당 매칭과 연결된 상품을 조회하고, 상품의 카테고리를 기준으로
	 * 동일하거나 더 높은 카테고리를 가진 장소들을 필터링한다. 필터링된 장소들은 게스트 사용자의 정보를 포함하여
	 * PlacesResponse 객체 리스트로 반환한다.</p>
	 *
	 * @param matchingId 매칭 ID (Long 타입)
	 * @param userId 현재 로그인한 사용자 ID
	 * @return 상품 카테고리에 맞는 장소 리스트를 담은 PlacesResponse 객체 리스트로 반환
	 * @Author thereisname
	 */
	@Transactional
	public BaseResponse<List<PlacesResponse>> getLocationOptionsForItem(Long matchingId, Long userId) {
		User user = findByUser(userId);

		Matching matching = findMatchingById(matchingId);
		Integer category = matching.getProduct().getCategory().getValue();

		List<PlacesResponse> places = placeRepository.findAll().stream()
			.filter(place -> place.getCategory().getValue() <= category)
			.map(place -> PlacesResponse.from(place, user))
			.collect(Collectors.toList());

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
		Place place = findPlaceById(placeId);
		return baseResponseService.getSuccessResponse(PlaceDetailResponse.from(place));
	}

	/**
	 * <p>주어진 요청 데이터를 사용하여 새로운 장소를 생성하고 성공 메시지를 반환합니다.</p>
	 *
	 * <p>이 메서드는 사용자 ID를 기반으로 장소를 생성하며, 이미 동일한 사용자 ID로 등록된 장소가 있을 경우
	 * 예외를 발생시킵니다. 또한, 이미지 업로드를 처리하며, 이미지가 없거나 유효하지 않을 경우에도 예외를 발생시킵니다.</p>
	 *
	 * @param placeRequest 장소 생성에 필요한 요청 데이터 (PlaceRequest 타입)
	 * @param userId 현재 로그인한 사용자 Id
	 * @return 성공적으로 생성된 장소에 대한 성공 메시지를 반환합니다.
	 * @throws CustomRuntimeException 동일한 사용자 ID로 이미 장소가 등록경우, 이미지가 유효하지 않거나 누락된 경우 발생합니다.
	 * @Author thereisname
	 */
	@Transactional
	public BaseResponse<String> createPlace(PlaceRequest placeRequest, Long userId) {
		User user = findByUser(userId);

		validatePlaceDoesNotExist(user.getId());

		if (!s3ImageUpload.hasValidImages(placeRequest.getImageUrl())) {
			throw new CustomRuntimeException(ImageException.IMAGE_REQUIRED_FIELDS_EMPTY);
		};

		List<String> combinedImageUrls = s3ImageUpload.uploadImages(placeRequest.getImageUrl(), "place/" +user.getId());

		Place place = Place.from(placeRequest, user, combinedImageUrls);

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
	 * @param userId 현재 로그인한 사용자 Id
	 * @return 장소 수정에 대한 성공 메시지를 반환합니다.
	 * @throws CustomRuntimeException 해당 사용자 ID로 등록된 장소가 존재하지 않는 경우 PLACE_NOT_FOUND 예외가 발생합니다.
	 * @Author thereisname
	 */
	@Transactional
	public BaseResponse<String> updatePlace(PlaceUpdateRequest placeRequest, Long userId) {
		User user = findByUser(userId);

		Place place = findPlaceByUserId(user.getId());

		List<String> updatedImages = s3ImageUpload.updateImageSet(
			placeRequest.getDeleteImageUrl(), placeRequest.getNewImageUrl(),
			"place/" + user.getId(), place.getImageUrl());

		updateUserLocationIfChanged(user, placeRequest);
		place.updateFields(placeRequest, updatedImages);

		return baseResponseService.getSuccessResponse("장소 수정 성공!");
	}

	private User findByUser(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));
	}

	private Matching findMatchingById(Long matchingId) {
		return matchingRepository.findById(matchingId)
			.orElseThrow(() -> new CustomRuntimeException(MatchingException.MATCHING_NOT_FOUND));
	}

	private Product findProductById(Long productId) {
		return productRepository.findById(productId)
			.orElseThrow(() -> new CustomRuntimeException(ProductException.PRODUCT_NOT_FOUND));
	}

	private Place findPlaceById(Long placeId) {
		return placeRepository.findById(placeId)
			.orElseThrow(() -> new CustomRuntimeException(PlaceException.PLACE_NOT_FOUND));
	}

	private Place findPlaceByUserId(Long userId) {
		return placeRepository.findByUserId(userId)
			.orElseThrow(() -> new CustomRuntimeException(PlaceException.PLACE_NOT_FOUND));
	}

	private void validatePlaceDoesNotExist(Long userId) {
		placeRepository.findByUserId(userId).ifPresent(p -> {
			throw new CustomRuntimeException(PlaceException.PLACE_ALREADY_EXISTS);
		});
	}

	// task: 장소 위치 변경 로직
	private void updateUserLocationIfChanged(User user, PlaceUpdateRequest placeRequest) {
		if (!user.getLocation().equals(placeRequest.getLocation())) {
			Map<String, Double> coordinates = locationTransform.getCoordinates(placeRequest.getLocation());
			user.updateLocation(placeRequest.getLocation(), coordinates);
		}
	}
}
