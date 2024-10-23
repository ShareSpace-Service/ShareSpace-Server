package com.sharespace.sharespace_server.user.dto;

import org.springframework.web.multipart.MultipartFile;
import com.sharespace.sharespace_server.global.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {

    private MultipartFile image;

    @NotNull
    @NotEmpty(message = "빈 값을 입력할 수 없습니다.")
    @NotBlank(message = "공백일 수 없습니다.")
    private String nickName;

    @NotNull
    @NotEmpty(message = "빈 값을 입력할 수 없습니다.")
    @NotBlank(message = "공백일 수 없습니다.")
    private String location;
}
