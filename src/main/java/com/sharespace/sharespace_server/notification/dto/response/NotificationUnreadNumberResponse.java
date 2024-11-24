package com.sharespace.sharespace_server.notification.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NotificationUnreadNumberResponse {
	int unreadNotificationNum;
}