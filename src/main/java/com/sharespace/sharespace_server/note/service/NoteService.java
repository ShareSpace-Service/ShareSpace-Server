package com.sharespace.sharespace_server.note.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sharespace.sharespace_server.global.enums.NotificationMessage;
import com.sharespace.sharespace_server.global.enums.Role;
import com.sharespace.sharespace_server.global.exception.CustomRuntimeException;
import com.sharespace.sharespace_server.global.exception.error.NoteException;
import com.sharespace.sharespace_server.global.exception.error.PlaceException;
import com.sharespace.sharespace_server.global.exception.error.UserException;
import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.global.response.BaseResponseService;
import com.sharespace.sharespace_server.matching.entity.Matching;
import com.sharespace.sharespace_server.matching.repository.MatchingRepository;
import com.sharespace.sharespace_server.note.dto.NoteDetailResponse;
import com.sharespace.sharespace_server.note.dto.NoteRequest;
import com.sharespace.sharespace_server.note.dto.NoteResponse;
import com.sharespace.sharespace_server.note.dto.NoteSenderListResponse;
import com.sharespace.sharespace_server.note.dto.NoteUnreadCountResponse;
import com.sharespace.sharespace_server.note.entity.Note;
import com.sharespace.sharespace_server.note.repository.NoteRepository;
import com.sharespace.sharespace_server.notification.service.NotificationService;
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
public class NoteService {
	final BaseResponseService baseResponseService;
	private final UserRepository userRepository;
	private final NoteRepository noteRepository;
	private final MatchingRepository matchingRepository;
	private final PlaceRepository placeRepository;
	private final ProductRepository productRepository;
	private final NotificationService notificationService;

	/**
	 * 로그인 사용자가 받은 모든 쪽지 리스트 조회
	 * <p>
	 *     현재 로그인된 사용자가 받은 모든 쪽지 리스트 형태로 반환
	 * </p>
	 *
	 * @param userId 현재 로그인한 사용자의 고유 ID
	 * @return 사용자가 받은 쪽지 리스트를 포함한 성공 응답
	 * @throws CustomRuntimeException 사용자가 존재하지 않는 경우
	 * @Author thereisname
	 */
	@Transactional
	public BaseResponse<List<NoteResponse>> getAllNotes(Long userId) {
		List<NoteResponse> noteResponsesList = noteRepository.findAllByReceiverId(userId).stream()
			.map(NoteResponse::toNoteResponse)
			.collect(Collectors.toList());

		return baseResponseService.getSuccessResponse(noteResponsesList);
	}

	/**
	 * 쪽지 전송
	 * <p>
	 *     로그인한 사용자를 기반으로 매칭이 성립된 사용작에게만 쪽지 전송
	 * </p>
	 *
	 * @param noteRequest 쪽지의 제목과 내용을 포함하는 요청 정보
	 * @param userId 현재 로그인한 사용자 ID (쪽지 발신자)
	 * @return 쪽지 전송 성공 메시지 응답
	 * @throws CustomRuntimeException 매칭 조건을 만족하지 않는 경우 또는 수신자가 없는 경우
	 * @Author thereisname
	 */
	@Transactional
	public BaseResponse<Void> createNote(NoteRequest noteRequest, Long userId) {
		User sender = findUserById(userId);
		User receiver = findUserById(noteRequest.getReceiverId());

		validateMatchingBetweenUsers(sender, receiver);

		Note note = Note.create(sender, receiver, noteRequest.getTitle(), noteRequest.getContent());
		noteRepository.save(note);

		// Receiver에게 알림 전송
		notificationService.sendNotification(receiver.getId(), NotificationMessage.RECEIVED_NOTE.format(sender.getNickName()));
		return baseResponseService.getSuccessResponse();
	}

	/**
	 * 쪽지 삭제
	 * <p>
	 *     noteId를 통해 데이터베이스에서 해당 쪽지의 존재 여부를 확인한 후, 쪽지 삭제
	 * </p>
	 *
	 * @param noteId 삭제하려는 쪽지의 고유 ID
	 * @return 쪽지 삭제 성공 메시지를 포함한 BaseResponse 객체
	 * @throws CustomRuntimeException noteId에 해당하는 쪽지가 존재하지 않을 경우 예외 발생
	 * @Author thereisname
	 */
	@Transactional
	public BaseResponse<Void> deleteNote(Long noteId) {
		Note note = findNoteById(noteId);
		noteRepository.delete(note);

		return baseResponseService.getSuccessResponse();
	}

	/**
	 * 특정 쪽지의 상세 내용 조회
	 *
	 * @param noteId 조회하려는 쪽지의 고유 ID
	 * @return 쪽지의 상세 정보를 포함한 BaseResponse 객체
	 * @throws CustomRuntimeException noteId에 해당하는 쪽지가 존재하지 않을 경우 예외 발생
	 * @Author thereisname
	 */
	@Transactional
	public BaseResponse<NoteDetailResponse> getNoteDetail(Long noteId) {
		Note note = findNoteById(noteId);
		NoteDetailResponse noteDetailResponse = NoteDetailResponse.from(note);

		return baseResponseService.getSuccessResponse(noteDetailResponse);
	}

	/**
	 * 쪽지 읽음 처리
	 *
	 * @param noteId 조회하고자 하는 쪽지 고유 ID
	 * @return 성공 여부 반환
	 * @Author thereisname
	 */
	@Transactional
	public BaseResponse<Void> markNoteAsRead(Long noteId) {
		Note note = findNoteById(noteId);
		note.setRead(true);
		return baseResponseService.getSuccessResponse();
	}

	/**
	 * 로그인 사용자가 쪽지를 보낼 수 있는 발신자 리스트 조회
	 * <p>
	 *     사용자의 역할(Role)에 따라 매칭된 사용자 리스트를 조회하여, 쪽지를 보낼 수 있는 발신자 리스트를 반환
	 * </p>
	 *
	 * @param userId 현재 로그인한 사용자의 고유 ID
	 * @return 발신 가능한 사용자 리스트를 포함한 BaseResponse 객체
	 * @Author thereisname
	 */
	@Transactional
	public BaseResponse<List<NoteSenderListResponse>> getSenderList(Long userId) {
		User user = findUserById(userId);
		List<NoteSenderListResponse> users = getUsersByRole(user);

		return baseResponseService.getSuccessResponse(users);
	}

	/**
	 * 받은 쪽지 중 안읽은 쪽지 개수 조회
	 *
	 * <p>Note 엔티티 컬럼 중 사용자가 받은 쪽지 중 is_read가 false인 값들의 개수를 조회</p>
	 *
	 * @param userId 현재 로그인한 사용자의 고유 ID
	 * @return 읽지 않은 쪽지 개수 반환
	 * @Author thereisname
	 */
	@Transactional
	public BaseResponse<NoteUnreadCountResponse> getUnreadNote(Long userId) {
		int unreadCount = noteRepository.findCountUnreadNotesByReceiverId(userId);
		return baseResponseService.getSuccessResponse(
			new NoteUnreadCountResponse(unreadCount)
		);
	}

	// task: 사용자가 존재하는지 검증하고 사용자 객체 반환
	private User findUserById(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));
	}

	// task: 쪽지가 존재하는지 확인하고 존재할 경우 해당 쪽지 반환
	private Note findNoteById(Long noteId) {
		return noteRepository.findById(noteId)
			.orElseThrow(() -> new CustomRuntimeException(NoteException.NOTE_NOT_FOUND));
	}

	// task: 발신자와 수신자 간의 매칭 관계 검증
	private void validateMatchingBetweenUsers(User sender, User receiver) {
		if (sender.getRole() == Role.ROLE_HOST && receiver.getRole() == Role.ROLE_GUEST) {
			validateMatchingForHostAndGuest(sender, receiver);
		}
		if (sender.getRole() == Role.ROLE_GUEST && receiver.getRole() == Role.ROLE_HOST) {
			validateMatchingForHostAndGuest(receiver, sender);
		}
	}

	// task: 호스트와 게스트 간 매칭 관계가 유효한지 검증
	private void validateMatchingForHostAndGuest(User host, User guest) {
		Long placeId = placeRepository.findByUserId(host.getId())
			.map(Place::getId)
			.orElseThrow(() -> new CustomRuntimeException(PlaceException.PLACE_NOT_FOUND));

		List<Long> productIds = productRepository.findAllByUserId(guest.getId()).stream()
			.map(Product::getId)
			.collect(Collectors.toList());

		List<Matching> matchingList = matchingRepository.findAllByProductIdInAndPlaceId(productIds, placeId);

		if (matchingList.isEmpty()) {
			throw new CustomRuntimeException(NoteException.NOTE_NOT_MATCHING);
		}
	}

	// task: 역할에 따라 발신 대상 사용자 정보를 반환
	private List<NoteSenderListResponse> getUsersByRole(User user) {
		return user.getRole() == Role.ROLE_HOST ? getHostUserMatchingGuests(user.getId()) : getGuestUserMatchingHosts(user.getId());
	}

	// task: 호스트 사용자가 발신할 수 있는 게스트 사용자 리스트 반환
	private List<NoteSenderListResponse> getHostUserMatchingGuests(Long hostId) {
		return matchingRepository.findAllByPlaceUserIdAndStatusIn(hostId)
			.stream()
			.map(matching -> matching.getProduct().getUser())
			.distinct()
			.map(NoteSenderListResponse::toNoteSenderListResponse)
			.collect(Collectors.toList());
	}

	// task: 게스트 사용자가 발신할 수 있는 호스트 사용자 리스트 반환
	private List<NoteSenderListResponse> getGuestUserMatchingHosts(Long guestId) {
		return matchingRepository.findAllByProductUserIdAndStatusIn(guestId)
			.stream()
			.map(matching -> matching.getPlace().getUser())
			.distinct()
			.map(NoteSenderListResponse::toNoteSenderListResponse)
			.collect(Collectors.toList());
	}
}
