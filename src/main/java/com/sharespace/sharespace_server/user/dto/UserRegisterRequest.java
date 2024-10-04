package com.sharespace.sharespace_server.user.dto;

import com.sharespace.sharespace_server.global.enums.Role;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterRequest {

    @NotNull
    @NotEmpty(message = "빈 값을 입력할 수 없습니다.")
    @NotBlank(message = "공백일 수 없습니다.")
    @Email(message = "이메일 형식이 유효하지 않습니다.")
    private String email;

    @NotNull
    private Role role;

    @NotNull
    @NotEmpty(message = "빈 값을 입력할 수 없습니다.")
    @NotBlank(message = "공백일 수 없습니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,20}$", message = "비밀번호 양식이 틀렸습니다.")
    private String password;


    @NotNull
    @NotEmpty(message = "빈 값을 입력할 수 없습니다.")
    @NotBlank(message = "공백일 수 없습니다.")
    private String passwordValidate;

    @NotNull
    @NotEmpty(message = "빈 값을 입력할 수 없습니다.")
    @NotBlank(message = "공백일 수 없습니다.")
    private String location;

    @NotNull
    @NotEmpty(message = "빈 값을 입력할 수 없습니다.")
    @NotBlank(message = "공백일 수 없습니다.")
    private String nickname;
}
