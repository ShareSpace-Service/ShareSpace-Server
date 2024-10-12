package com.sharespace.sharespace_server.notification.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

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
	public SseEmitter subscribe(Long userId) {
		SseEmitter emitter = new SseEmitter();
		// 기본적으로 연결 유지
		try {
			emitter.send(SseEmitter.event().name("INIT").data("SSE 연결됨"));
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
		Notification notification = new Notification();
		notification.setUser(userRepository.findById(userId)
			.orElseThrow(() -> new CustomRuntimeException(UserException.MEMBER_NOT_FOUND)));
		notification.setMessage(message);
		notification.setCreatedAt(LocalDateTime.now());
		notification.setRead(false);
		notificationRepository.save(notification);
		SseEmitter emitter = sseEmitters.get(userId);
		if (emitter != null) {
			try {
				emitter.send(SseEmitter.event().name("NOTIFICATION").data(message));
			} catch (IOException e) {
				emitter.completeWithError(e);
				sseEmitters.remove(userId);
			}
		}
	}
	
	// 메시지 알림 처리
	public void newMessage(Long userId, String messageContent) {
		String notificationMessage = "새로운 메시지가 도착했습니다: " + messageContent;
		sendNotification(userId, notificationMessage);
	}
}
