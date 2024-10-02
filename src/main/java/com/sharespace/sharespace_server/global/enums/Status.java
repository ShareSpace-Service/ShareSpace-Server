package com.sharespace.sharespace_server.global.enums;

public enum Status {
	UNASSIGNED, // 미배정 (장소 선택 안 하고 물품만 등록함)
	REQUESTED, // 요청됨 (물품 등록 + 장소 선택까지 함)
	REJECTED, // 반려됨 (호스트 측에서 거절)
	PENDING, // (보관) 대기중 -> 호스트가 장소 선택 후, 실질적인 보관은 하지 않은 대기 상태

	STORED, // 보관중
	COMPLETED // 완료됨
}
