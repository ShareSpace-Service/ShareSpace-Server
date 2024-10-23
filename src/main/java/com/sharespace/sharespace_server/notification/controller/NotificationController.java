package com.sharespace.sharespace_server.notification.controller;


import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.notification.dto.response.NotificationAllResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.sharespace.sharespace_server.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping(value = "/sse/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribe(@PathVariable Long userId) {
		return notificationService.subscribe(userId);
	}

	@GetMapping()
	public BaseResponse<List<NotificationAllResponse>> getNotifications() {
		Long userId = 1L;
		return notificationService.getNotifications(userId);
	}

	@DeleteMapping()
	BaseResponse<Void> deleteNotifcation(@RequestParam Long notificationId) {
		return notificationService.deleteNotifcation(notificationId);
	}
}
