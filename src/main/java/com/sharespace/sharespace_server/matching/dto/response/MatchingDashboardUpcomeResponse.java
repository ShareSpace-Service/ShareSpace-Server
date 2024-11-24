package com.sharespace.sharespace_server.matching.dto.response;

import com.sharespace.sharespace_server.global.enums.Category;
import com.sharespace.sharespace_server.global.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class MatchingDashboardUpcomeResponse {
    private Long matchingId;
    private String title;
    private Category category;
    private List<String> imageUrl; // product 이미지
    private Status status;
    private Integer distance;
    private Integer remainingDays;

}
