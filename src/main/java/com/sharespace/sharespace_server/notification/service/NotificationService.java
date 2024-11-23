package com.sharespace.sharespace_server.notification.service;

import static com.sharespace.sharespace_server.notification.entity.Notification.*;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.sharespace.sharespace_server.global.exception.error.NotificationException;
import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.global.response.BaseResponseService;
import com.sharespace.sharespace_server.notification.dto.response.NotificationAllResponse;
import com.sharespace.sharespace_server.notification.dto.response.NotificationUnreadNumberResponse;
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
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

	private final Map<Long, Set<SseEmitter>> userEmitters = new ConcurrentHashMap<>();
	private final UserRepository userRepository;
	private final NotificationRepository notificationRepository;
	private final BaseResponseService baseResponseService;
	private static final long TIMEOUT = 60 * 60 * 1000L; // 1시간

	/**
	 * SSE 구독 요청 처리
	 * @param userId 구독할 유저 ID
	 * @return SseEmitter 객체를 반환해서 SSE 연결
	 */
	public SseEmitter subscribe(Long userId) {
		SseEmitter emitter = new SseEmitter(TIMEOUT);
		
		// 사용자별 emitter Set을 가져오거나 새로 생성
		Set<SseEmitter> userEmitterSet = userEmitters.computeIfAbsent(userId, 
			k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
		userEmitterSet.add(emitter);

		// 연결시 deadEmitter 제거
		cleanDeadEmitters(userId, userEmitterSet);
		log.info("현재 userId = " + userId + " 연결된 SSE 개수 : " + userEmitterSet.size());
		// 연결 직후 첫 메시지 전송
		sendInitialMessage(emitter);
		
		// 연결 종료 시 처리
		emitter.onCompletion(() -> removeEmitter(userId, emitter));
		emitter.onTimeout(() -> removeEmitter(userId, emitter));
		emitter.onError(e -> removeEmitter(userId, emitter));
		
		return emitter;
	}

	private void removeEmitter(Long userId, SseEmitter emitter) {
		Set<SseEmitter> userEmitterSet = userEmitters.get(userId);
		if (userEmitterSet != null) {
			userEmitterSet.remove(emitter);
			// Set이 비어있으면 Map에서 제거
			if (userEmitterSet.isEmpty()) {
				userEmitters.remove(userId);
			}
		}
	}

	private void sendInitialMessage(SseEmitter emitter) {
		try {
			emitter.send(SseEmitter.event()
				.name("INIT")
				.data("Connected!"));
		} catch (IOException e) {
			emitter.complete();
			log.error("초기 메시지 전송 실패: {}", e.getMessage());
		}
	}

	/**
	 * 유저에게 알림을 보내는 메서드
	 * @param userId 알림을 받을 유저 ID
	 * @param message 알림 메시지 내용
	 */
	@Transactional
	public void sendNotification(Long userId, String message) {
		Set<SseEmitter> deadEmitters = new HashSet<>();
		Set<SseEmitter> userEmitterSet = userEmitters.get(userId);
		User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));

		if (userEmitterSet != null) {
			Notification notification = create(user, message);
			notificationRepository.save(notification);
			userEmitterSet.forEach(emitter -> {
				try {
					emitter.send(SseEmitter.event()
						.id(userId.toString())
						.name("NOTIFICATION")
						.data(message));

				} catch (IOException e) {
					deadEmitters.add(emitter);
					// log.error("Dead Emiiter는 제거됩니다. {}", e.getMessage());
				}
			});
			
			// 실패한 emitter 제거
			deadEmitters.forEach(emitter -> removeEmitter(userId, emitter));
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

	@Transactional
	public BaseResponse<Void> readAllNotifications(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));
		List<Notification> notifications = notificationRepository.findAllByUser(user);
		for (Notification notification : notifications) {
			notification.setRead(true);
		}
		return baseResponseService.getSuccessResponse();
	}

	public void sendNotificationTest() {
		Long userId = 1L;
		sendNotification(userId, "테스트 알림입니다.");
	}

	public BaseResponse<NotificationUnreadNumberResponse> getUnreadNotifcationNumber(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));
		int unreadNotificationsCount = notificationRepository.findUnreadNotificationsCountByUser(user);

		return baseResponseService.getSuccessResponse(
			NotificationUnreadNumberResponse.builder()
				.unreadNotificationNum(unreadNotificationsCount)
				.build());
	}

	// 알림 모두 지우기
	@Transactional
	public BaseResponse<Void> deleteAllNotifications(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));
		notificationRepository.deleteAllByUser(user);
		return baseResponseService.getSuccessResponse();
	}

	/**
	 * Dead Emitter 제거 메서드
	 * @param userId 유저 ID
	 * @param emitters 유저의 SseEmitter Set
	 */
	private void cleanDeadEmitters(Long userId, Set<SseEmitter> emitters) {
		for (SseEmitter emitter : emitters) {
			try {
				emitter.send(SseEmitter.event()
					.name("HEARTBEAT_CHECK")
					.data("Checking emitter health"));
			} catch (IOException e) {
				removeEmitter(userId, emitter);
			}
		}
	}
 	// 로그아웃시 호출될 메서드
	public void removeAllEmittersByUserId(Long userId) {
		Set<SseEmitter> userEmitterSet = userEmitters.get(userId);
		if (userEmitterSet != null) {
			userEmitterSet.forEach(emitter -> {
				try {
					emitter.complete();
				} catch (Exception e) {
					log.error("Error while completing emitter for user {}: {}", userId, e.getMessage());
				}
			});
			userEmitters.remove(userId);
			log.info("Removed all SSE connections for user {}", userId);
		}
	}


}

