package com.sharespace.sharespace_server.user.dto;

import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이내로 작성해주세요.")
    private String nickName;

    @NotNull
    @NotEmpty(message = "빈 값을 입력할 수 없습니다.")
    @NotBlank(message = "공백일 수 없습니다.")
    private String location;
}
