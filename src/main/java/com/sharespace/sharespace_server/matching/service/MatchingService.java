package com.sharespace.sharespace_server.matching.service;


import com.sharespace.sharespace_server.global.utils.S3ImageUpload;
import com.sharespace.sharespace_server.matching.dto.MatchingAssembler;
import com.sharespace.sharespace_server.matching.dto.request.MatchingGuestConfirmStorageRequest;
import com.sharespace.sharespace_server.matching.dto.request.MatchingHostAcceptRequestRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import com.sharespace.sharespace_server.global.enums.Status;
import com.sharespace.sharespace_server.global.exception.CustomRuntimeException;
import com.sharespace.sharespace_server.global.exception.error.MatchingException;
import com.sharespace.sharespace_server.global.exception.error.PlaceException;
import com.sharespace.sharespace_server.global.exception.error.ProductException;
import com.sharespace.sharespace_server.global.exception.error.UserException;
import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.global.response.BaseResponseService;
import com.sharespace.sharespace_server.matching.dto.request.MatchingKeepRequest;
import com.sharespace.sharespace_server.matching.dto.request.MatchingUploadImageRequest;
import com.sharespace.sharespace_server.matching.dto.response.MatchingShowAllResponse;
import com.sharespace.sharespace_server.matching.dto.response.MatchingShowKeepDetailResponse;
import com.sharespace.sharespace_server.matching.dto.response.MatchingShowRequestDetailResponse;
import com.sharespace.sharespace_server.matching.entity.Matching;
import com.sharespace.sharespace_server.matching.repository.MatchingRepository;
import com.sharespace.sharespace_server.notification.service.NotificationService;
import com.sharespace.sharespace_server.place.dto.MatchingPlaceDto;
import com.sharespace.sharespace_server.place.dto.PlaceRequestedDetailResponse;
import com.sharespace.sharespace_server.place.entity.Place;
import com.sharespace.sharespace_server.place.repository.PlaceRepository;
import com.sharespace.sharespace_server.product.dto.MatchingProductDto;
import com.sharespace.sharespace_server.product.dto.ProductRequestedDetailResponse;
import com.sharespace.sharespace_server.product.entity.Product;
import com.sharespace.sharespace_server.product.repository.ProductRepository;
import com.sharespace.sharespace_server.user.entity.User;
import com.sharespace.sharespace_server.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static com.sharespace.sharespace_server.global.enums.NotificationMessage.*;
import static com.sharespace.sharespace_server.global.enums.Status.*;

@Service
@RequiredArgsConstructor
public class MatchingService {

	private final NotificationService notificationService;
	private final BaseResponseService baseResponseService;
	private final MatchingRepository matchingRepository;
	private final ProductRepository productRepository;
	private final PlaceRepository placeRepository;
	private final UserRepository userRepository;
	private final S3ImageUpload s3ImageUpload;
	private final MatchingAssembler matchingAssembler;

	/**
	 * 모든 매칭을 조회하여 응답 객체로 반환하는 메서드
	 *
	 * 1. 사용자 ID를 기반으로 사용자를 조회하고,
	 * 2. 사용자의 모든 물품을 조회하여, 각 물품에 대해 매칭 응답을 생성하여 리스트로 반환
	 *
	 * @param userId - 조회할 사용자의 ID
	 * @return BaseResponse<List<MatchingShowAllResponse>> - 모든 매칭 정보 리스트를 담은 응답 객체
	 */
	
	
	public BaseResponse<List<MatchingShowAllResponse>> showAll(Long userId) {
		User user = findUser(userId);
		List<Matching> matchings = matchingRepository.findMatchingWithProductByUserId(userId);
		List<MatchingShowAllResponse> responses = matchings.stream()
			.map(matchingAssembler::toMatchingShowAllResponse)
			.collect(Collectors.toList());
		return baseResponseService.getSuccessResponse(responses);
	}
	/**s
	 * MatchingKeepRequest 객체를 기반으로 Place와 Product를 매칭하여 Matching 엔티티를 생성하는 메서드
	 *
	 * 1. Place와 Product의 유효성을 검사하고,
	 * 2. Product의 카테고리 값이 Place의 카테고리 값보다 클 경우 예외를 던지며,
	 * 3. 이미 동일한 Product와 Place로 매칭이 존재하는 경우 예외를 던짐
	 * 4. Matching을 생성하고, Product의 isPlaced 상태를 true로 업데이트
	 *
	 * @param request - 매칭을 위한 Place와 Product의 ID를 포함하는 요청 객체
	 * @return BaseResponse<Void> - 성공 시 응답 객체 (데이터 없음)
	 * @throws CustomRuntimeException - Place나 Product가 존재하지 않거나, 카테고리 불일치 또는 매칭 중복 시 발생
	 */
	@Transactional
	public BaseResponse<Void> keep(MatchingKeepRequest request) {
		// Place와 Product를 찾고, 유효성 검증
		Place place = placeRepository.findById(request.getPlaceId())
			.orElseThrow(() -> new CustomRuntimeException(PlaceException.PLACE_NOT_FOUND));
		Product product = productRepository.findById(request.getProductId())
			.orElseThrow(() -> new CustomRuntimeException(ProductException.PRODUCT_NOT_FOUND));

		// Product와 Place에서 Category와 Period에 대해 유효성 검증 수행
		product.validateCategoryForPlace(place);
		product.validatePeriodForPlace(place);

		// 매칭 생성 및 처리 로직은 Matching 객체가 처리
		Matching matching = Matching.create(product, place);
		matchingRepository.save(matching);

		// 요청받은 Host에게 알림 전송
		notificationService.sendNotification(place.getUser().getId(), REQUEST_KEEPING_TO_HOST.getMessage());

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

	// FIXME : 매칭 Status에 대한 검증 로직이 필요할 것 같음
	public BaseResponse<MatchingShowKeepDetailResponse> showKeepDetail(Long matchingId) {
		Matching matching = findMatching(matchingId);

		MatchingProductDto matchingProductDto = MatchingProductDto.from(matching.getProduct());
		MatchingPlaceDto matchingPlaceDto = MatchingPlaceDto.from(matching.getPlace());

		// matching의 상태가 '보관중', '보관 대기중'일 경우, 이 response 반환
		MatchingShowKeepDetailResponse response = MatchingShowKeepDetailResponse.builder()
			.product(matchingProductDto)
			.place(matchingPlaceDto)
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
	@Transactional
	public BaseResponse<Void> completeStorage(Long matchingId) {
		User user = findUser(1L); // TODO : 토큰에서 유저 가져오는 것으로 변경

		Matching matching = findMatching(matchingId);

		matching.completeStorage(user);

		matchingRepository.save(matching);
		// TODO : 알림 전송 로직 및 한 쪽만 수락했을 때 반대쪽에게 알림 전송
		// 알림 전송 로직

		// Host만 수락했을 때
		if (matching.isHostCompleted() && !matching.isGuestCompleted()) {
			notificationService.sendNotification(matching.getProduct().getUser().getId(),
				HOST_COMPLETED_KEEPING.format(matching.getProduct().getUser().getNickName()));
		}
		// Guest만 수락했을 때
		if (!matching.isHostCompleted() && matching.isGuestCompleted()) {
			notificationService.sendNotification(matching.getPlace().getUser().getId(),
				GUEST_COMPLETED_KEEPING.format(matching.getPlace().getUser().getNickName()));
		}

		// 둘 다 수락됐으면
		if (matching.isHostCompleted() && matching.isGuestCompleted()){
			// TODO : 최적화 필요할 듯, 메서드 두 번 호출이 아니라 다중 userId를 Arguments로 받을 수 있게끔
			notificationService.sendNotification(matching.getProduct().getUser().getId(),
				BOTH_COMPLETED_KEEPING.getMessage());
			notificationService.sendNotification(matching.getPlace().getUser().getId(),
				BOTH_COMPLETED_KEEPING.getMessage());
		}
		return baseResponseService.getSuccessResponse();
	}


	/**
	 * 주어진 matchingId에 해당하는 매칭 상세 정보를 조회하는 메서드
	 *
	 * 1. Matching 엔티티를 조회하고,
	 * 2. Product와 Place 정보를 DTO로 변환하여 응답
	 *
	 * @param matchingId - 조회할 매칭 엔티티의 ID
	 * @return BaseResponse<MatchingShowRequestDetailResponse> - 매칭 상세 정보를 담은 응답 객체
	 */

	public BaseResponse<MatchingShowRequestDetailResponse> showRequestDetail(Long matchingId) {
		Matching matching = findMatching(matchingId);

		PlaceRequestedDetailResponse placeResponse = PlaceRequestedDetailResponse.of(matching.getPlace());
		ProductRequestedDetailResponse productResponse = ProductRequestedDetailResponse.of(matching.getProduct());
		MatchingShowRequestDetailResponse response = MatchingShowRequestDetailResponse.builder()
			.place(placeResponse)
			.product(productResponse)
			.build();


		return baseResponseService.getSuccessResponse(response);
	}

	/**
	 * 호스트가 매칭 요청을 수락 또는 거절하는 메서드
	 * Matching의 상태가 REQUESTED -> PENDING
	 *
	 * @param request - 호스트가 매칭 요청을 수락/거절하는 요청 객체
	 * @return BaseResponse<Void> - 성공 시 응답 객체
	 */
	@Transactional
	public BaseResponse<Void> hostAcceptRequest(MatchingHostAcceptRequestRequest request) {
		// TODO : 호스트 유효성 검증 필요
		// TODO : 메서드 리네임 => 거절할 수도 있고 수락할 수도 있으니까
		Matching matching = findMatching(request.getMatchingId());
		if (request.isAccepted()) {
			matching.setStatus(PENDING);
			// 알림 전송 로직. Guest에게 알림을 전송한다.
			notificationService.sendNotification(matching.getProduct().getUser().getId(),
				HOST_ACCEPTED_MATCHING_REQUEST.getMessage());
		} else {
			matching.setStatus(Status.REJECTED);
			notificationService.sendNotification(matching.getProduct().getUser().getId(),
				HOST_REJECETED_MATCHING_REQUEST.getMessage());
		}
		matchingRepository.save(matching);
		return baseResponseService.getSuccessResponse();
	}

	/**
	 * 게스트가 물품 보관을 확인처리 (보관 대기중 -> 보관중)
	 *
	 * 1. 매칭의 상태가 PENDING인지 확인하고,
	 * 2. PENDING 상태일 경우 물품 보관 상태로 변경
	 *
	 * @param request - 매칭 ID를 포함하는 요청 객체
	 * @return BaseResponse<Void> - 성공 시 응답 객체
	 * @throws CustomRuntimeException - 매칭 상태가 PENDING이 아니거나 유효하지 않은 경우 발생
	 */
	@Transactional
	public BaseResponse<Void> guestConfirmStorage(MatchingGuestConfirmStorageRequest request) {
		Matching matching = findMatching(request.getMatchingId());

		matching.confirmStorageByGuest();

		matchingRepository.save(matching);
		// TODO : IMAGE가 Null일 때 예외처리?
		return baseResponseService.getSuccessResponse();
	}


	/**
	 * 매칭 요청을 취소하는 메서드
	 *
	 * 1. 사용자 ID를 통해 사용자를 조회하고,
	 * 2. 매칭 상태가 PENDING인 경우에만 요청을 취소하며,
	 * 3. GUEST가 요청을 취소하는 경우 미배정 상태로 변경하고, HOST가 요청을 취소하는 경우 반려 상태로 변경
	 *
	 * @param matchingId - 취소할 매칭 엔티티의 ID
	 * @return BaseResponse<Void> - 성공 시 응답 객체
	 * @throws CustomRuntimeException - 매칭 상태가 PENDING이 아니거나 유효하지 않은 경우 발생
	 */
	@Transactional
	public BaseResponse<Void> cancelRequest(Long matchingId) {
		User user = findUser(1L); // TODO : 토큰에서 유저 가져오는 것으로 변경
		Long userId = user.getId(); // TODO : 매직 넘버를 사용했으므로 나중엔 Parameter에서 받아와야 함
		Matching matching = findMatching(matchingId);

		// Matching 객체에게 취소 처리 위임
		matching.cancel(user);

		matchingRepository.save(matching);
		
		
		// 알림 전송 로직
		// 1. 요청 취소 주체 찾기 => userId
			/* 1-1. 요청 취소의 주체가 Place의 owner, 즉, Host일 경우
			 * matching.getProduct().getUser.getId() => Guest의 userId로 알림 전송
			 */
		if (userId == matching.getPlace().getUser().getId()) {
			notificationService.sendNotification(matching.getProduct().getUser().getId(), CANCELED_MATCHING.getMessage());
		} else {
			/* 1-2. 요청 취소의 주체가 Product의 owner, 즉, Host일 경우
			 * matching.getPlace().getUser.getId() => Host의 userId로 알림 전송
			 */
			notificationService.sendNotification(matching.getPlace().getUser().getId(), CANCELED_MATCHING.getMessage());
		}
		return baseResponseService.getSuccessResponse();
	}

	/**
	 * 호스트가 이미지 업로드
	 *
	 * 1. 주어진 매칭 ID에 해당하는 매칭 엔티티를 조회하고,
	 * 2. 이미지 유효성을 검사한 후,
	 * 3. 이미지를 S3에 업로드하고 매칭 엔티티에 이미지 URL을 설정
	 *
	 * @param request - 이미지 업로드 요청 객체 (매칭 ID 및 이미지 URL 포함)
	 * @return BaseResponse<Void> - 성공 시 응답 객체
	 * @throws CustomRuntimeException - 이미지 유효성 검사 실패 시 발생
	 */

	@Transactional
	public BaseResponse<Void> uploadImage(MatchingUploadImageRequest request) {
		// TODO : 유효성 검증;이미지 업로드의 주체는 Host여야 함
		// NOTE : 이미지 업로드'만' 했다고 알림이 전송되어선 안됨
		Matching matching = findMatching(request.getMatchingId());
		// 다중 이미지 S3에 업로드
		String imageUrl = s3ImageUpload.uploadSingleImage(request.getImageUrl(), "matching/" +matching.getId());
		matching.setImage(imageUrl);
		matchingRepository.save(matching);

		return baseResponseService.getSuccessResponse();
	}

	/**
	 * 주어진 매칭 ID에 해당하는 매칭 엔티티를 조회하는 메서드
	 *
	 * @param matchingId - 조회할 매칭 엔티티의 ID
	 * @return Matching - 매칭 엔티티
	 * @throws CustomRuntimeException - 매칭 ID가 유효하지 않은 경우 예외 발생
	 */
	public Matching findMatching(Long matchingId) {
		return matchingRepository.findById(matchingId)
				.orElseThrow(() -> new CustomRuntimeException(MatchingException.MATCHING_NOT_FOUND));
	}

	public User findUser(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));
	}
}