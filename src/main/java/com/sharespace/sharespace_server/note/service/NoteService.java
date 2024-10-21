package com.sharespace.sharespace_server.note.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sharespace.sharespace_server.global.enums.NotificationMessage;
import com.sharespace.sharespace_server.global.enums.Role;
import com.sharespace.sharespace_server.global.enums.Status;
import com.sharespace.sharespace_server.global.exception.CustomRuntimeException;
import com.sharespace.sharespace_server.global.exception.error.NoteException;
import com.sharespace.sharespace_server.global.exception.error.UserException;
import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.global.response.BaseResponseService;
import com.sharespace.sharespace_server.matching.entity.Matching;
import com.sharespace.sharespace_server.matching.repository.MatchingRepository;
import com.sharespace.sharespace_server.note.dto.NoteDetailResponse;
import com.sharespace.sharespace_server.note.dto.NoteRequest;
import com.sharespace.sharespace_server.note.dto.NoteResponse;
import com.sharespace.sharespace_server.note.dto.NoteSenderListResponse;
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

	@Transactional
	public BaseResponse<List<NoteResponse>> getNote() {
		User user = findUserById(2L);

		List<NoteResponse> noteResponsesList = noteRepository.findAllByReceiverId(user.getId()).stream()
			.map(NoteResponse::toNoteResponse)
			.collect(Collectors.toList());

		return baseResponseService.getSuccessResponse(noteResponsesList);
	}

	@Transactional
	public BaseResponse<String> createNote(NoteRequest noteRequest) {
		User sender = findUserById(1L);
		User receiver = findUserById(noteRequest.getReceiverId());

		validateMatchingBetweenUsers(sender, receiver);

		Note note = Note.create(sender, receiver, noteRequest.getTitle(), noteRequest.getContent());
		noteRepository.save(note);

		// Receiver에게 알림 전송
		notificationService.sendNotification(receiver.getId(), NotificationMessage.RECEIVED_NOTE.format(sender.getNickName()));
		return baseResponseService.getSuccessResponse("쪽지 보내기 성공!");
	}

	@Transactional
	public BaseResponse<String> deleteNote(Long noteId) {
		Note note = findNoteById(noteId);
		noteRepository.delete(note);

		return baseResponseService.getSuccessResponse("쪽지 삭제 성공");
	}

	@Transactional
	public BaseResponse<NoteDetailResponse> getNoteDetail(Long noteId) {
		Note note = findNoteById(noteId);
		NoteDetailResponse noteDetailResponse = NoteDetailResponse.from(note);

		return baseResponseService.getSuccessResponse(noteDetailResponse);
	}

	@Transactional
	public BaseResponse<List<NoteSenderListResponse>> getSenderList() {
		User user = findUserById(1L);
		List<Long> userIds = getUserIdsByRole(user);

		if (userIds.isEmpty()) {
			throw new CustomRuntimeException(NoteException.SENDER_NOT_FOUND);
		}

		List<NoteSenderListResponse> users = userRepository.findAllById(userIds)
			.stream()
			.map(NoteSenderListResponse::toNoteSenderListResponse)
			.toList();

		return baseResponseService.getSuccessResponse(users);
	}

	private User findUserById(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));
	}

	private Note findNoteById(Long noteId) {
		return noteRepository.findById(noteId)
			.orElseThrow(() -> new CustomRuntimeException(NoteException.NOTE_NOT_FOUND));
	}

	private void validateMatchingBetweenUsers(User sender, User receiver) {
		if (sender.getRole() == Role.ROLE_HOST && receiver.getRole() == Role.ROLE_GUEST) {
			validateMatchingForHostAndGuest(sender, receiver);
		}
		if (sender.getRole() == Role.ROLE_GUEST && receiver.getRole() == Role.ROLE_HOST) {
			validateMatchingForHostAndGuest(receiver, sender);
		}
	}

	private void validateMatchingForHostAndGuest(User host, User guest) {
		Long placeId = placeRepository.findByUserId(host.getId())
			.map(Place::getId)
			.orElseThrow(() -> new CustomRuntimeException(NoteException.NOTE_NOT_MATCHING));

		List<Long> productIds = productRepository.findAllByUserId(guest.getId()).stream()
			.map(Product::getId)
			.collect(Collectors.toList());

		List<Matching> matchingList = matchingRepository.findAllByProductIdInAndPlaceId(productIds, placeId);

		if (matchingList.isEmpty()) {
			throw new CustomRuntimeException(NoteException.NOTE_NOT_MATCHING);
		}
	}

	private List<Long> getUserIdsByRole(User user) {
		return user.getRole() == Role.ROLE_HOST ? getUserIdsForHost(user) : getUserIdsForGuest(user);
	}

	private List<Long> getUserIdsForHost(User user) {
		return matchingRepository.findAllByPlaceUserIdAndStatusIn(user.getId(), List.of(Status.PENDING, Status.STORED))
			.stream()
			.map(matching -> matching.getProduct().getUser().getId())
			.distinct()
			.collect(Collectors.toList());
	}

	private List<Long> getUserIdsForGuest(User user) {
		return matchingRepository.findAllByProductUserIdAndStatusIn(user.getId(), List.of(Status.PENDING, Status.STORED))
			.stream()
			.map(matching -> matching.getPlace().getUser().getId())
			.distinct()
			.collect(Collectors.toList());
	}
}
