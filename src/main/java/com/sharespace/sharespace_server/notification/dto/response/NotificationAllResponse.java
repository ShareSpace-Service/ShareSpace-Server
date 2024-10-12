package com.sharespace.sharespace_server.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class NotificationAllResponse {
    private Long notifcationId;
    private String message;
    private Integer timeElapsed;
    private boolean isRead;
}
