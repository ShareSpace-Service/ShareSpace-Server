package com.sharespace.sharespace_server.notification;

import static com.sharespace.sharespace_server.global.enums.NotificationMessage.*;
import static com.sharespace.sharespace_server.global.enums.Status.*;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sharespace.sharespace_server.matching.entity.Matching;
import com.sharespace.sharespace_server.matching.repository.MatchingRepository;
import com.sharespace.sharespace_server.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {
	private final NotificationService notificationService;
	private final MatchingRepository matchingRepository;
	public static final int CONFIRM_DAYS = 7;
	@Scheduled(cron = "0 0 0 * * ?") // 매일 밤 자정에 실행한다는 의미의 크론식
	public void sendExpiryNotifcations() {
		LocalDateTime today = LocalDateTime.now();
		List<Matching> matchingList = matchingRepository.findEligibleForNotification(STORED, CONFIRM_DAYS, today);


		for (Matching matching : matchingList) {
			// 물품 보관이 CONFIRM_DAYS일이 지났다면 알림 전송 (Guest에게만)
			if (matching.getStatus().equals(STORED) &&
				matching.getStartDate().plusDays(CONFIRM_DAYS).isEqual(today)) {
				notificationService.sendNotification(matching.getProduct().getUser().getId(), KEEPING_STATUS_ALERT_TO_GUEST.format(
					String.valueOf(CONFIRM_DAYS)));
			}
			
			// 곧 만기일이 다가온다는 알림 수신
			if (matching.getStatus().equals(STORED) &&
				matching.getExpiryDate().minusDays(CONFIRM_DAYS).isEqual(today)) {
				notificationService.sendNotification(matching.getProduct().getUser().getId(), EXPIRATION_APPROACH_WARNING.getMessage());
				notificationService.sendNotification(matching.getPlace().getUser().getId(), EXPIRATION_APPROACH_WARNING.getMessage());
			}

			// 만기일에 알림 수신
			if (matching.getStatus().equals(STORED) &&
				matching.getExpiryDate().isEqual(today)) {
				notificationService.sendNotification(matching.getProduct().getUser().getId(), KEEPING_EXPIRED_NOTIFICATION_TO_GUEST.getMessage());
				notificationService.sendNotification(matching.getPlace().getUser().getId(), KEEPING_EXPIRED_NOTIFICATION_TO_HOST.getMessage());
			}
		}
	}
}
