package com.sharespace.sharespace_server.global.utils;

public class DistanceUtils {
	private static final double EARTH_RADIUS = 6371000; // 지구 반지름 (미터)

	public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
			+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
			* Math.sin(dLon / 2) * Math.sin(dLon / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return EARTH_RADIUS * c; // 거리 (미터)
	}

	public static int calculateRoundedDistance(double lat1, double lon1, double lat2, double lon2) {
		double distance = calculateDistance(lat1, lon1, lat2, lon2);
		return (int) Math.round(distance); // 거리 반올림 후 정수로 변환
	}
}
