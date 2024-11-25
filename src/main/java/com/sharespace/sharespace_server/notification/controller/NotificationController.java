package com.sharespace.sharespace_server.notification.controller;


import static com.sharespace.sharespace_server.global.utils.RequestParser.*;

import com.sharespace.sharespace_server.global.annotation.CheckPermission;
import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.global.utils.RequestParser;
import com.sharespace.sharespace_server.notification.dto.response.NotificationAllResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.sharespace.sharespace_server.notification.dto.response.NotificationUnreadNumberResponse;
import com.sharespace.sharespace_server.notification.service.NotificationService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	@CheckPermission(roles = {"ROLE_GUEST", "ROLE_HOST"})
	public SseEmitter subscribe(HttpServletRequest request) {
		Long userId = RequestParser.extractUserId(request);
		return notificationService.subscribe(userId);
	}

	@GetMapping()
	@CheckPermission(roles = {"ROLE_GUEST", "ROLE_HOST"})
	public BaseResponse<List<NotificationAllResponse>> getNotifications(
		HttpServletRequest request,
		@RequestParam int page,
		@RequestParam int size) {
		Long userId = extractUserId(request);
		return notificationService.getNotifications(userId, page, size);
	}
	@DeleteMapping()
	@CheckPermission(roles = {"ROLE_GUEST", "ROLE_HOST"})
	BaseResponse<Void> deleteNotifcation(@RequestParam Long notificationId) {
		return notificationService.deleteNotifcation(notificationId);
	}

	@PatchMapping("/read")
	@CheckPermission(roles = {"ROLE_GUEST", "ROLE_HOST"})
	BaseResponse<Void> readAllNotifications(HttpServletRequest request) {
		Long userId = extractUserId(request);
		return notificationService.readAllNotifications(userId);
	}

	@GetMapping("/unread-alarms")
	@CheckPermission(roles = {"ROLE_GUEST", "ROLE_HOST"})
	BaseResponse<NotificationUnreadNumberResponse> getUnreadNotificationNumber(HttpServletRequest request) {
		Long userId = extractUserId(request);
		return notificationService.getUnreadNotifcationNumber(userId);
	}

	@DeleteMapping("/all")
	@CheckPermission(roles = {"ROLE_GUEST", "ROLE_HOST"})
	BaseResponse<Void> deleteAllNotifications(HttpServletRequest request) {
		Long userId = extractUserId(request);
		return notificationService.deleteAllNotifications(userId);
	}


	@PostMapping("/send")
	void sendNotifcationsTest() {
		notificationService.sendNotificationTest();
	}

}
