	package com.sharespace.sharespace_server.matching.service;

	import java.time.LocalDateTime;

	import com.sharespace.sharespace_server.global.enums.Category;
	import com.sharespace.sharespace_server.global.exception.error.ImageException;
	import com.sharespace.sharespace_server.global.utils.S3ImageUpload;
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

	import static com.sharespace.sharespace_server.global.enums.Status.*;
	import static com.sharespace.sharespace_server.global.utils.LocationTransform.*;

	@Service
	@RequiredArgsConstructor
	public class MatchingService {

		private final BaseResponseService baseResponseService;
		private final MatchingRepository matchingRepository;
		private final ProductRepository productRepository;
		private final PlaceRepository placeRepository;
		private final UserRepository userRepository;
		private final S3ImageUpload s3ImageUpload;

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
			User user = userRepository.findById(userId)
				.orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));
			List<Product> products = productRepository.findAllByUserId(userId);
			System.out.println(products);
			List<MatchingShowAllResponse> responses = products.stream()
				.map(this::createMatchingResponse)
				.collect(Collectors.toList());
			return baseResponseService.getSuccessResponse(responses);
		}
		/**
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
			// Place와 Product를 찾고, 유효성 검사
			Place place = placeRepository.findById(request.getPlaceId())
			.orElseThrow(() -> new CustomRuntimeException(PlaceException.PLACE_NOT_FOUND));
			Product product = productRepository.findById(request.getProductId())
				.orElseThrow(() -> new CustomRuntimeException(ProductException.PRODUCT_NOT_FOUND));

			// 2024-10-04 : Product의 카테고리 값이 Place보다 클 경우 예외처리
			if (place.getCategory().getValue() < product.getCategory().getValue()) {
				throw new CustomRuntimeException(MatchingException.CATEGORY_NOT_MATCHED);
			}

			// 2024-10-04 : 이미 존재하는 Matching의 경우 예외처리
			if (matchingRepository.findByProductIdAndPlaceId(product.getId(), place.getId()).isPresent()) {
				throw new CustomRuntimeException(MatchingException.ALREADY_EXISTED_MATCHING);
			}

			// 2024-10-07 : Period 에외처리, place의 period는 product의 period보다 커선 안 된다.
			if (place.getPeriod() < product.getPeriod()) {
				throw new CustomRuntimeException(MatchingException.INVALID_PRODUCT_PERIOD);
			}

			User host = place.getUser();
			User guest = product.getUser();

			// 두 Host와 Guest의 거리 계산
			Integer distance = calculateDistance(guest.getLatitude(), guest.getLongitude(),
				host.getLatitude(), host.getLongitude());

			// Matching 엔티티 생성 후 필요한 값 설정
			Matching matching = new Matching();
			matching.setProduct(product);
			matching.setPlace(place);
			matching.setStatus(Status.REQUESTED);
			matching.setStartDate(LocalDateTime.now());
			matching.setDistance(distance);


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
			// TODO : 알림 기능 추가하기
			User user = userRepository.findById(1L) // TODO: 토큰에서 유저 정보 받아오는 것으로 변경 예정
				.orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));

			Matching matching = findMatching(matchingId);
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
				.placeRequestedDetailResponse(placeResponse)
				.productRequestedDetailResponse(productResponse)
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
			Matching matching = findMatching(request.getMatchingId());
			if (request.isAccepted()) {
				matching.setStatus(PENDING);
			} else {
				matching.setStatus(Status.REJECTED);
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

			// 예외처리 1. Matching의 Status가 PENDING(보관 대기중)이어야 올바른 응답을 반환해야함
			if (!matching.getStatus().equals(PENDING)) {
				throw new CustomRuntimeException(MatchingException.INCORRECT_STATUS_CONFIRM_REQUEST_GUEST);
			}

			// NOTE : Matching의 ImageUrl이 null일 때 예외처리를 해줘야할까?

			matching.setStatus(STORED);
			matchingRepository.save(matching);

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
			User user = userRepository.findById(1L)
				.orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));


			Matching matching = findMatching(matchingId);

			if (!matching.getStatus().equals(PENDING)) {
				throw new CustomRuntimeException(MatchingException.REQUEST_CANCELLATION_NOT_ALLOWED);
			}
			Product product = matching.getProduct();
			// 요청 취소시, 해당 product의 is_placed 컬럼을 false로 업데이트한다.
			product.setIsPlaced(false);

			// GUEST가 요청 취소를 눌렀을 때의 플로우
			if (user.getRole().getValue().equals("GUEST")) {
				matching.setStatus(UNASSIGNED); // Matching의 상태를 미배정 상태로 변경
			}

			// HOST가 요청 취소를 눌렀을 때의 플로우
			if (user.getRole().getValue().equals("HOST")) {
				matching.setStatus(Status.REJECTED); // Matching의 상태를 반려됨으로 변경
			}

			// TODO : 취소 당한 상대에게 알림이 전송되어야 함
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
			// TODO : 이미지 업로드의 주체는 Host여야 한다.
			Matching matching = findMatching(request.getMatchingId());
			if (!s3ImageUpload.hasValidImages(request.getImageUrl())) {
				throw new CustomRuntimeException(ImageException.IMAGE_REQUIRED_FIELDS_EMPTY);
			};
			// 다중 이미지 S3에 업로드
			List<String> combinedImageUrls = s3ImageUpload.uploadImages(request.getImageUrl(), "matching/" +request.getMatchingId());
			matching.setImage(String.join(",", combinedImageUrls));
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

		/**
		 * 게스트 유효성을 검증하는 메서드
		 *
		 * 1. 게스트가 해당 물품을 가지고 있는지 확인
		 * 2. 이미 완료된 상태인지 확인
		 *
		 * @param matching - 매칭 엔티티
		 * @param user - 검증할 게스트 사용자
		 * @throws CustomRuntimeException - 물품이 유저에 속하지 않거나 이미 완료된 상태인 경우 예외 발생
		 */
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

		/**
		 * 호스트 유효성을 검증하는 메서드
		 *
		 * 1. 호스트가 해당 장소를 가지고 있는지 확인
		 * 2. 이미 완료된 상태인지 확인
		 *
		 * @param matching - 매칭 엔티티
		 * @param user - 검증할 호스트 사용자
		 * @throws CustomRuntimeException - 장소가 유저에 속하지 않거나 이미 완료된 상태인 경우 예외 발생
		 */
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

		/**
		 * 매칭된 모든 물품 정보를 조회하여 응답 객체로 빌드하는 메서드
		 *
		 * @param matchingId - 매칭된 물품의 ID
		 * @param title - 물품 제목
		 * @param category - 카테고리
		 * @param imageUrl - 이미지 URL 리스트
		 * @param status - 매칭 상태
		 * @param distance - 거리 정보
		 * @return MatchingShowAllResponse - 매칭 정보 응답 객체
		 */

		private MatchingShowAllResponse buildMatchingShowAllResponse(Long matchingId, String title, Category category, List<String> imageUrl, Status status, Integer distance) {
			return MatchingShowAllResponse.builder()
				.matchingId(matchingId)
				.title(title)
				.category(category)
				.imageUrl(imageUrl)
				.status(status)
				.distance(distance)
				.build();
		}


		/**
		 * 주어진 물품을 기반으로 매칭 응답을 생성하는 메서드
		 *
		 * @param product - 매칭 응답을 생성할 물품 엔티티
		 * @return MatchingShowAllResponse - 매칭 정보 응답 객체
		 */
		private MatchingShowAllResponse createMatchingResponse(Product product) {
			Long productId = product.getId();
			List<String> imageUrls = List.of(product.getImageUrl().split(","));
			if (!product.getIsPlaced()) {
				// isPlaced가 false인 경우
				return buildMatchingShowAllResponse(null, product.getTitle(), product.getCategory(), imageUrls, UNASSIGNED, null);
			} else {
				// isPlaced가 true인 경우
				Matching matching = matchingRepository.findByProductId(productId);
				return buildMatchingShowAllResponse(matching.getId(), product.getTitle(), product.getCategory(), imageUrls,
					matching.getStatus(),matching.getDistance());
			}
		}
	}
