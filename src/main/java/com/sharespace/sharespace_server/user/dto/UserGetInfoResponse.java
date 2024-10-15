package com.sharespace.sharespace_server.user.dto;

import com.sharespace.sharespace_server.global.enums.Role;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserGetInfoResponse {
    private String nickName;
    private String email;
    private String image;
    private String role;
    private String location;
}
