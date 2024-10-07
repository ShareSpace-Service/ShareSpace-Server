package com.sharespace.sharespace_server.global.utils;

import com.sharespace.sharespace_server.global.exception.CustomRuntimeException;
import com.sharespace.sharespace_server.global.exception.error.LocationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class LocationTransform {
    @Value("${kakao.url}")
    private String apiUrl;

    @Value("${kakao.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public LocationTransform() {
        this.restTemplate = new RestTemplate();
    }

    private static final double EARTH_RADIUS = 6371000; // 지구 반지름 (미터)

    public Map<String, Double> getCoordinates(String roadName) {
        // URI 빌더를 사용하여 쿼리 파라미터 추가
        String uri = apiUrl + roadName;

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // API 호출
        ResponseEntity<Map> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                Map.class
        );

        // 응답에서 위도와 경도 추출
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> body = response.getBody();
            if (body.containsKey("documents")) {
                Object documentsObj = body.get("documents");
                if (documentsObj instanceof java.util.List) {
                    java.util.List documents = (java.util.List) documentsObj;
                    if (!documents.isEmpty()) {
                        Map firstDocument = (Map) documents.get(0);
                        Double longitude = Double.valueOf((String) firstDocument.get("x"));
                        Double latitude = Double.valueOf((String) firstDocument.get("y"));
                        return Map.of("latitude", latitude, "longitude", longitude);
                    }
                }
            }
        }
        throw new CustomRuntimeException(LocationException.FETCH_FAIL);
    }

    /**
     * 두 지점의 위도와 경도를 받아 거리를 계산한 후, 반올림하여 미터 단위의 정수값으로 반환합니다.
     * <p>
     * 사용 방법:
     * <pre>{@code
     * // LocationTransform 인스턴스를 생성합니다.
     * private final LocationTransform locationTransform;
     *
     * // calculateDistance 메서드를 호출하여 두 지점 간의 거리를 계산합니다.
     * Integer distance = locationTransform.calculateDistance(lat1, lon1, lat2, lon2);
     *
     * }</pre>
     * </p>
     *
     * @param lat1 Guest Latitude(위도)
     * @param lon1 Guest Longitude(경도)
     * @param lat2 Host Latitude(위도)
     * @param lon2 Host Longitude(경도)
     * @return 거리를 계산 후 정수의 값으로 반올림한 값
     */
    public static Integer calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // Haversine 공식 적용
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 지구 반지름을 곱하여 거리 계산, 결과값을 반올림하여 반환
        return (int) Math.round(EARTH_RADIUS * c);
    }
}
