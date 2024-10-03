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
}
