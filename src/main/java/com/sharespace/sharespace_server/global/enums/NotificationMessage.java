package com.sharespace.sharespace_server.global.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationMessage {
	// Guest로부터 물품 보관 요청을 받음
	REQUEST_KEEPING_TO_HOST("새로운 물품 보관 요청이 도착했습니다. 확인해주세요."),
	// 물품 보관 완료 요청 - HOST가 먼저 완료, GUEST가 먼저 완료
	HOST_COMPLETED_KEEPING("Host가 보관 완료처리를 진행하였습니다. %s님께서 확인 부탁드립니다."),
	GUEST_COMPLETED_KEEPING("Guest가 보관 완료처리를 진행하였습니다. %s님께서 확인 부탁드립니다."),
	BOTH_COMPLETED_KEEPING("Host, Guest 양 측에서 보관 완료 처리되었습니다. 물품 반환이 완료되었습니다. 감사합니다."),
	HOST_UPLOADED_IMAGE_TO_MATCHING("Host가 물품 보관 이미지를 업로드하였습니다. 확인 후 수락 버튼을 눌러주세요"),
	CANCELED_MATCHING("상대방이 보관 요청을 취소하였습니다."), // 요청을 “취소”한다
	HOST_REJECETED_MATCHING_REQUEST("Host측에서 물품 보관 요청을 거절하였습니다."),
	HOST_ACCEPTED_MATCHING_REQUEST("Host가 물품 보관 요청을 수락했습니다."),
	KEEPING_STATUS_ALERT_TO_GUEST("물품을 맡기신 지 %d일이 경과했습니다. 물품 상태를 확인해주세요."),
	EXPIRATION_APPROACH_WARNING("물품 보관 만기일이 %일 남았습니다."),
	KEEPING_EXPIRED_NOTIFICATION_TO_HOST("보관 만기일이 오늘입니다. 물품 인계 후 보관 완료 버튼을 눌러주세요. 미완료 시 자동 반납 처리됩니다."),
	KEEPING_EXPIRED_NOTIFICATION_TO_GUEST("보관 만기일이 오늘입니다. 물품 인수 후 보관 완료 버튼을 눌러주세요. 미완료 시 자동 반납 처리됩니다."),
	RECEIVED_NOTE("%s님으로부터 쪽지가 도착하였습니다.");

	private final String message;

	// 동적 데이터를 포맷팅하기 위한 메서드
	public String format(String... args) {
		return String.format(this.message, args);
	}
}

