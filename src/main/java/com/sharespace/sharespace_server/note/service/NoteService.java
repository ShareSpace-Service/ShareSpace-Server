package com.sharespace.sharespace_server.note.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

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

	@Transactional
	public BaseResponse<List<NoteResponse>> getNote() {
		// user Id의 값을 가져오는 과정. 추후 Token으로 user을 추정하여 id값을 가져올 예정
		User user = userRepository.findById(2L)
			.orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));

		List<NoteResponse> noteResponsesList = noteRepository.findAllByReceiverId(user.getId()).stream()
			.map(note -> NoteResponse.builder()
				.noteId(note.getId())
				.title(note.getTitle())
				.content(note.getContent())
				.sender(note.getSender().getNickName())
				.build())
			.collect(Collectors.toList());

		if (noteResponsesList.isEmpty()) {
			throw new CustomRuntimeException(NoteException.NOTE_NOT_FOUND);
		}

		return baseResponseService.getSuccessResponse(noteResponsesList);
	}

	@Transactional
	public BaseResponse<String> createNote(NoteRequest noteRequest) {
		// User Id 불러오기
		User sender = userRepository.findById(4L)
			.orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));

		User receiver = userRepository.findById(noteRequest.getReceiverId())
			.orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));

		if (noteRequest.getTitle().isEmpty() || noteRequest.getContent().isEmpty()) {
			throw new CustomRuntimeException(NoteException.NOTE_TITLE_ANE_CONTENT_EMPTY);
		}

		if (noteRequest.getReceiverId() == null) {
			throw new CustomRuntimeException(NoteException.RECEIVER_NOT_FOUND);
		}

		Long placeUserId = sender.getRole().equals(Role.ROLE_HOST) ? sender.getId() : receiver.getId();
		Long placeId = placeRepository.findByUserId(placeUserId)
			.orElseThrow(() -> new CustomRuntimeException(NoteException.NOTE_NOT_MATCHING)).getId();

		Long productUserId = sender.getRole().equals(Role.ROLE_HOST) ? receiver.getId() : sender.getId();
		List<Long> productIds = productRepository.findAllByUserId(productUserId).stream()
			.map(Product::getId)
			.toList();

		Matching matching = matchingRepository.findByProductIdInAndPlaceId(productIds, placeId);

		if (matching == null) {
			throw new CustomRuntimeException(NoteException.NOTE_NOT_MATCHING);
		}

		Note note = Note.builder()
			.sender(sender)
			.receiver(receiver)
			.title(noteRequest.getTitle())
			.content(noteRequest.getContent())
			.send_at(LocalDateTime.now())
			.build();

		noteRepository.save(note);

		return baseResponseService.getSuccessResponse("쪽지 보내기 성공!");
	}

	@Transactional
	public BaseResponse<String> deleteNote(Long noteId) {
		// 쪽지 존재 여부 확인
		if (noteRepository.existsById(noteId)) {
			throw new CustomRuntimeException(NoteException.NOTE_NOT_FOUND);
		}

		noteRepository.deleteById(noteId);

		return baseResponseService.getSuccessResponse("쪽지 삭제 성공");
	}

	@Transactional
	public BaseResponse<NoteDetailResponse> getNoteDetail(Long noteId) {
		Note note = noteRepository.findById(noteId)
			.orElseThrow(() -> new CustomRuntimeException(NoteException.NOTE_NOT_FOUND));

		NoteDetailResponse noteDetailResponse = NoteDetailResponse.builder()
			.noteId(noteId)
			.title(note.getTitle())
			.content(note.getContent())
			.sender(note.getSender().getNickName())
			.senderTime(note.getSend_at().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
			.build();

		return baseResponseService.getSuccessResponse(noteDetailResponse);
	}

	@Transactional
	public BaseResponse<List<NoteSenderListResponse>> getSenderList() {
		User user = userRepository.findById(1L)
			.orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));

		List<Long> userIds = getUserIdsByRole(user);
		if (userIds.isEmpty()) {
			throw new CustomRuntimeException(NoteException.SENDER_NOT_FOUND);
		}

		List<NoteSenderListResponse> users = userRepository.findAllById(userIds)
			.stream()
			.map(sender -> NoteSenderListResponse.builder()
				.receiverId(sender.getId())
				.nickname(sender.getNickName())
				.build())
			.toList();

		return baseResponseService.getSuccessResponse(users);
	}

	private List<Long> getUserIdsByRole(User user) {
		if (user.getRole().equals(Role.ROLE_HOST)) {
			return getUserIdsForHost(user);
		} else if (user.getRole().equals(Role.ROLE_GUEST)) {
			return getUserIdsForGuest(user);
		}
		return Collections.emptyList();
	}

	private List<Long> getUserIdsForHost(User user) {
		// user가 등록한 place 테이블 내 placeId 불러오기
		List<Long> placesId = placeRepository.findAllByUserId(user.getId())
			.stream()
			.map(Place::getId)
			.collect(Collectors.toList());

		// place Id가 matching table 내 있는지 확인. 단, status가 '보관 대기중', '보관중' 인 경우에만 가능
		List<Matching> matchings = matchingRepository.findAllByPlaceIdInAndStatusIn(
			placesId,
			Arrays.asList(Status.PENDING, Status.STORED)
		);

		// 찾은 matching Id를 바탕으로 product Id를 찾아 guest 정보 가져오기
		return matchings.stream()
			.map(matching -> matching.getProduct().getUser().getId())
			.distinct()
			.collect(Collectors.toList());
	}

	private List<Long> getUserIdsForGuest(User user) {
		// user가 등록한 product 테이블 내 productId 불러오기
		List<Long> productId = productRepository.findAllByUserId(user.getId())
			.stream()
			.map(Product::getId)
			.toList();

		// place Id가 matching table 내 있는지 확인. 단, status가 '보관 대기중', '보관중' 인 경우에만 가능
		List<Matching> matchings = matchingRepository.findAllByProductIdInAndStatusIn(
			productId,
			Arrays.asList(Status.PENDING, Status.STORED)
		);

		// 찾은 matching Id를 바탕으로 product Id를 찾아 guest 정보 가져오기
		return matchings.stream()
			.map(matching -> matching.getPlace().getUser().getId())
			.distinct()
			.collect(Collectors.toList());
	}
}
