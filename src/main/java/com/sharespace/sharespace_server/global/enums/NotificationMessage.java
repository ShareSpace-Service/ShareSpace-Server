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
	RECEIVED_NOTE("%s님으로부터 쪽지가 도착하였습니다.");

	private final String message;

	// 동적 데이터를 포맷팅하기 위한 메서드
	public String format(String... args) {
		return String.format(this.message, args);
	}
}

