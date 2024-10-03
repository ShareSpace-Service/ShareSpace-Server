package com.sharespace.sharespace_server.global.utils;

public class DistanceUtils {
	private static final double EARTH_RADIUS = 6371000; // 지구 반지름 (미터)

	/**
	 * 게스트, 호스트 각각의 위도, 경도를 받아 두 사람의 거리를 계산하는 로직
	 *
	 * @param lat1 Guest Latitude(위도)
	 * @param lon1 Guest Longitude(경도)
	 * @param lat2 Host Latitude(위도)
	 * @param lon2 Host Longitude(경도)
	 * @return 거리를 계산 후 정수의 값으로 반올림한 값
	 */
	public static int calculateDistance(double lat1, double lon1, double lat2, double lon2) {
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
			+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
			* Math.sin(dLon / 2) * Math.sin(dLon / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return (int) Math.round(EARTH_RADIUS * c); // 거리 (미터)
	}
}
