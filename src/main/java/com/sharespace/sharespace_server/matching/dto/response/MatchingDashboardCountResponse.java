package com.sharespace.sharespace_server.matching.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MatchingDashboardCountResponse {
    private Integer requestedCount;
    private Integer pendingCount;
    private Integer storedCount;
}
