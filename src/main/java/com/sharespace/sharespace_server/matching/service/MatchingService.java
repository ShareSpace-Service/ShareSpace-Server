package com.sharespace.sharespace_server.matching.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.sharespace.sharespace_server.global.enums.Status;
import com.sharespace.sharespace_server.global.exception.CustomRuntimeException;
import com.sharespace.sharespace_server.global.exception.error.MatchingException;
import com.sharespace.sharespace_server.global.exception.error.PlaceException;
import com.sharespace.sharespace_server.global.exception.error.ProductException;
import com.sharespace.sharespace_server.global.exception.error.UserException;
import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.global.response.BaseResponseService;
import com.sharespace.sharespace_server.matching.dto.request.MatchingKeepRequest;
import com.sharespace.sharespace_server.matching.dto.response.MatchingShowKeepDetailResponse;
import com.sharespace.sharespace_server.matching.entity.Matching;
import com.sharespace.sharespace_server.matching.repository.MatchingRepository;
import com.sharespace.sharespace_server.place.dto.MatchingPlaceResponse;
import com.sharespace.sharespace_server.place.entity.Place;
import com.sharespace.sharespace_server.place.repository.PlaceRepository;
import com.sharespace.sharespace_server.product.dto.MatchingProductDto;
import com.sharespace.sharespace_server.product.entity.Product;
import com.sharespace.sharespace_server.product.repository.ProductRepository;
import com.sharespace.sharespace_server.user.entity.User;
import com.sharespace.sharespace_server.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchingService {

	private final BaseResponseService baseResponseService;
	private final MatchingRepository matchingRepository;
	private final ProductRepository productRepository;
	private final PlaceRepository placeRepository;
	private final UserRepository userRepository;


	/**
	 * MatchingKeepRequest 객체를 기반으로 Place와 Product를 매칭하여 Matching 엔티티를 생성하는 메서드
	 *
	 * 1. Place와 Product의 유효성을 검사하고,
	 * 2. Product의 카테고리 값이 Place의 카테고리 값보다 클 경우 예외를 던지며,
	 * 3. 이미 동일한 Product와 Place로 매칭이 존재하는 경우 예외를 던짐
	 * 4. Matching을 생성하고, Product의 isPlaced 상태를 true로 업데이트
	 *
	 * @param matchingKeepRequest - 매칭을 위한 Place와 Product의 ID를 포함하는 요청 객체
	 * @return BaseResponse<Void> - 성공 시 응답 객체 (데이터 없음)
	 * @throws CustomRuntimeException - Place나 Product가 존재하지 않거나, 카테고리 불일치 또는 매칭 중복 시 발생
	 */
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

	/**
	 * 주어진 matchingId에 해당하는 Matching 엔티티를 조회하여, 그에 따른 상세 정보를 반환하는 메서드
	 *
	 * 1. Matching 엔티티를 데이터베이스에서 조회하고,
	 * 2. Product와 Place를 DTO로 변환한 후,
	 * 3. Matching 상태가 '보관중' 또는 '보관 대기중'일 경우, MatchingShowKeepDetailResponse를 반환
	 *
	 * @param matchingId - 조회할 Matching 엔티티의 ID
	 * @return BaseResponse<MatchingShowKeepDetailResponse> - 매칭 상세 정보를 담은 응답 객체
	 * @throws CustomRuntimeException - 주어진 matchingId에 해당하는 매칭을 찾을 수 없는 경우 예외 발생
	 */
	public BaseResponse<MatchingShowKeepDetailResponse> showKeepDetail(Long matchingId) {
		Matching matching = matchingRepository.findById(matchingId)
			.orElseThrow(() -> new CustomRuntimeException(MatchingException.MATCHING_NOT_FOUND));

		MatchingProductDto matchingProductDto = MatchingProductDto.from(matching.getProduct());
		MatchingPlaceResponse matchingPlaceResponse = MatchingPlaceResponse.from(matching.getPlace());

		// matching의 상태가 '보관중', '보관 대기중'일 경우, 이 response 반환
		MatchingShowKeepDetailResponse response = MatchingShowKeepDetailResponse.builder()
			.product(matchingProductDto)
			.place(matchingPlaceResponse)
			.imageUrl(matching.getImage())
			.build();

		return baseResponseService.getSuccessResponse(response);
	}

	/**
	 * 주어진 matchingId에 해당하는 매칭 정보를 통해 물품 보관 완료 처리를 하는 메서드
	 *
	 * 1. 매칭된 Guest 또는 Host의 완료 여부를 확인한 후, 매칭 테이블에서 해당 유저(호스트/게스트)의 완료 상태를 업데이트
	 * 2. Guest와 Host 모두가 완료한 경우, 매칭 상태를 'COMPLETED'로 변경
	 * 3. 매칭 ID가 유효하지 않거나, 유저 권한이 맞지 않는 경우에는 예외를 던짐
	 *
	 * @param matchingId - 완료 처리할 매칭 엔티티의 ID
	 * @return BaseResponse<Void> - 성공 시 응답 객체 (데이터 없음)
	 * @throws CustomRuntimeException - 유효하지 않은 매칭 ID이거나, 유저 권한 또는 상태가 일치하지 않는 경우 예외 발생
	 */
	public BaseResponse<Void> completeStorage(Long matchingId) {
		// TODO : 알림 기능 추가하기
		User user = userRepository.findById(1L) // TODO: 토큰에서 유저 정보 받아오는 것으로 변경 예정
			.orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));

		Matching matching = matchingRepository.findById(matchingId)
			.orElseThrow(() -> new CustomRuntimeException(MatchingException.MATCHING_NOT_FOUND));

		// user의 권한을 확인한다. HOST인지, GUEST인지에 따라 동작이 바뀐다.
		if (user.getRole().getValue().equals("GUEST")) {
			validateGuest(matching, user);
			matching.setGuestCompleted(true);
		}
		else if (user.getRole().getValue().equals("HOST")) {
			validateHost(matching, user);
			matching.setGuestCompleted(true);
		}

		if (matching.isGuestCompleted() && matching.isHostCompleted()) {
			matching.setStatus(Status.COMPLETED);
		} // TODO : 한 쪽만 누른 상황일 때 다른 한 쪽에게 알림 전송
		matchingRepository.save(matching);

		return baseResponseService.getSuccessResponse();
	}

	private void validateGuest(Matching matching, User user) {
		// 게스트가 물건을 가지고 있는지 확인
		if (!matching.getProduct().getUser().equals(user)) {
			throw new CustomRuntimeException(MatchingException.MATCHING_PRODUCT_NOT_IN_USER);
		}
		// 이미 완료된 상태인지 확인
		if (matching.isGuestCompleted()) {
			throw new CustomRuntimeException(MatchingException.GUEST_ALREADY_COMPLETED_KEEPING);
		}
	}

	private void validateHost(Matching matching, User user) {
		// 호스트가 장소를 가지고 있는지 확인
		if (!matching.getPlace().getUser().equals(user)) {
			throw new CustomRuntimeException(MatchingException.MATCHING_PLACE_NOT_IN_USER);
		}
		// 이미 완료된 상태인지 확인
		if (matching.isHostCompleted()) {
			throw new CustomRuntimeException(MatchingException.HOST_ALREADY_COMPLETED_KEEPING);
		}
	}

	public BaseResponse<Void> showRequestDetail(Long matchingId) {
	}
}
