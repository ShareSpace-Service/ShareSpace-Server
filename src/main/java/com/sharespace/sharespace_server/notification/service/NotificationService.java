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
import com.sharespace.sharespace_server.user.entity.User;
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
	public SseEmitter subscribe(Long userId) {
		SseEmitter emitter = new SseEmitter();
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
		emitter.onTimeout(() -> sseEmitters.remove(userId));

		return emitter;
	}

	@Transactional
	public void sendNotification(Long userId, String message)  {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));
		Notification notification = create(user, message);
		notificationRepository.save(notification);
		SseEmitter emitter = sseEmitters.get(userId);
		if (emitter != null) {
			try {
				emitter.send(SseEmitter.event()
					.name("NOTIFICATION")
					.data(message));
			} catch (IOException e) {
				emitter.completeWithError(e);
				sseEmitters.remove(userId);
			}
		} // TODO : emitter가 존재하지 않을 때 예외처리 필요
	}

	public BaseResponse<List<NotificationAllResponse>> getNotifications(Long userId) {
		User user = userRepository.findById(userId).
		orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND));
		List<Notification> notificationList = notificationRepository.findAllByUser(user);

		List<NotificationAllResponse> response = new ArrayList<>();
		LocalDateTime now = LocalDateTime.now();
		for (Notification notification : notificationList) {
			Duration duration = Duration.between(notification.getCreatedAt(), now);
			int timeElapsed = (int) duration.toHours();

			NotificationAllResponse notificationResponse = NotificationAllResponse.builder()
					.notifcationId(notification.getId())
					.isRead(notification.isRead())
					.timeElapsed(timeElapsed)
					.message(notification.getMessage())
					.build();

			response.add(notificationResponse);
		}
		return baseResponseService.getSuccessResponse(response);
	}

	public BaseResponse<Void> deleteNotifcation(Long notificationId) {
		notificationRepository.findById(notificationId)
			.orElseThrow(() -> new CustomRuntimeException(NotificationException.NOTIFCATION_NOT_FOUND));

		notificationRepository.deleteById(notificationId);
		return baseResponseService.getSuccessResponse();
	}
}
