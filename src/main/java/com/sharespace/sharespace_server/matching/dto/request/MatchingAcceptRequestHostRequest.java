package com.sharespace.sharespace_server.matching.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MatchingAcceptRequestHostRequest {

    @NotNull
    private Long matchingId;
    @NotNull
    private boolean isAccepted;
}
