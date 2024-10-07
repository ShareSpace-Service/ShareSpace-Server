package com.sharespace.sharespace_server.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserEmailValidateRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Integer validationNumber;
}
