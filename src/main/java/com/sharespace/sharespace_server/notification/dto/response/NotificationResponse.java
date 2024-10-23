package com.sharespace.sharespace_server.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class NotificationResponse {
	private Long notificationId; // 알림 ID
	private String message; // 알림 메시지
}
