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

    @NotNull(message = "userId는 null이 되어선 안 됩니다.")
    private Long userId;

    private Integer validationNumber;
}
