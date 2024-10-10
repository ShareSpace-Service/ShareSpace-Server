package com.sharespace.sharespace_server.notification.service;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final ConcurrentHashMap<Long, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
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


	public void sendNotification(Long userId, String message)  {
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
}
