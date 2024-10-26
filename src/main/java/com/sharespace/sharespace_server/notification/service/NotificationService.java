package com.sharespace.sharespace_server.notification.service;

import static com.sharespace.sharespace_server.notification.entity.Notification.*;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.sharespace.sharespace_server.global.exception.error.NotificationException;
import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.global.response.BaseResponseService;
import com.sharespace.sharespace_server.notification.dto.response.NotificationAllResponse;
import com.sharespace.sharespace_server.notification.dto.response.NotificationResponse;
import com.sharespace.sharespace_server.user.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.sharespace.sharespace_server.global.exception.CustomRuntimeException;
import com.sharespace.sharespace_server.global.exception.error.UserException;
import com.sharespace.sharespace_server.notification.entity.Notification;
import com.sharespace.sharespace_server.notification.repository.NotificationRepository;
import com.sharespace.sharespace_server.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final ConcurrentHashMap<Long, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
	private final UserRepository userRepository;
	private final NotificationRepository notificationRepository;
	private final BaseResponseService baseResponseService;

	/**
	 * SSE 구독 요청 처리
	 * @param userId 구독할 유저 ID
	 * @return SseEmitter 객체를 반환해서 SSE 연결
	 */
	public SseEmitter subscribe(Long userId) {
		SseEmitter emitter = new SseEmitter(30000000L);
		sseEmitters.put(userId, emitter);
		// 기본적으로 연결 유지
		try {
			emitter.send(SseEmitter.event()
				.name("INIT")
				.data("SSE 연결됨"));
		} catch(IOException e) {
			emitter.completeWithError(e);
		}
		// 연결이 종료되면 emitter를 제거
		emitter.onCompletion(() -> sseEmitters.remove(userId));
		emitter.onTimeout(() -> {
			removeSseEmitter(userId);
			}
		);

		return emitter;
	}

	/**
	 * 유저에게 알림을 보내는 메서드
	 * @param userId 알림을 받을 유저 ID
	 * @param message 알림 메시지 내용
	 */
	@Transactional
	public void sendNotification(Long userId, String message)  {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));
		Notification notification = create(user, message);
		notificationRepository.save(notification);
		SseEmitter emitter = sseEmitters.get(userId);
		if (emitter != null) {
			try {
				NotificationResponse response = NotificationResponse.builder()
					.notificationId(notification.getId())
					.message(message)
					.build();
				emitter.send(SseEmitter.event()
					.name("NOTIFICATION")
					.data(response));
			} catch (IOException e) {
				emitter.completeWithError(e);
				sseEmitters.remove(userId);
			}
		}
	}

	/**
	 * 유저의 모든 알림을 가져오는 메서드
	 * @param userId 알림을 조회할 유저 ID
	 * @return 알림 리스트와 함께 성공 응답을 반환
	 */
	public BaseResponse<List<NotificationAllResponse>> getNotifications(Long userId, int page, int size) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));

		PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")); // 최신 알림이 위로 오도록 정렬
		Page<Notification> notificationPage = notificationRepository.findAllByUser(user, pageRequest);

		List<NotificationAllResponse> response = new ArrayList<>();
		LocalDateTime now = LocalDateTime.now();
		for (Notification notification : notificationPage.getContent()) {
			Duration duration = Duration.between(notification.getCreatedAt(), now);
			int timeElapsed = (int) duration.toMinutes();

			NotificationAllResponse notificationResponse = NotificationAllResponse.builder()
				.notificationId(notification.getId())
				.isRead(notification.isRead())
				.timeElapsed(timeElapsed)
				.message(notification.getMessage())
				.build();

			response.add(notificationResponse);
		}
		return baseResponseService.getSuccessResponse(response);
	}


	/**
	 * 알림을 삭제하는 메서드
	 * @param notificationId 삭제할 알림 ID
	 * @return 삭제 후 성공 응답 반환
	 */
	public BaseResponse<Void> deleteNotifcation(Long notificationId) {
		notificationRepository.findById(notificationId)
			.orElseThrow(() -> new CustomRuntimeException(NotificationException.NOTIFCATION_NOT_FOUND));

		notificationRepository.deleteById(notificationId);
		return baseResponseService.getSuccessResponse();
	}

	public void removeSseEmitter(Long userId) {
		SseEmitter emitter = sseEmitters.remove(userId);
		if (emitter != null) {
			emitter.complete();
		}
	}
}
