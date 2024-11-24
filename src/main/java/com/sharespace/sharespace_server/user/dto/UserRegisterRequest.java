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
    @NotEmpty(message = "Email은 필수 입력값 입니다.")
    @NotBlank(message = "Email란은 공백일 수 없습니다.")
    @Email(message = "이메일 형식이 유효하지 않습니다.")
    private String email;

    @NotNull
    private Role role;

    @NotNull
    @NotEmpty(message = "비밀번호를 필수로 입력해주세요.")
    @NotBlank(message = "비밀번호란은 공백일 수 없습니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,20}$", message = "비밀번호 양식이 틀렸습니다.")
    private String password;

    @NotNull
    @NotEmpty(message = "비밀번호 검증을 위해 확인란을 필수로 입력해주세요.")
    @NotBlank(message = "비밀번호 확인란은 공백일 수 없습니다.")
    private String passwordValidate;

    @NotNull
    @NotEmpty(message = "주소를 필수로 입력해주세요.")
    @NotBlank(message = "주소란은 공백일 수 없습니다.")
    private String location;

    @NotNull
    @NotEmpty(message = "회원명을 입력해주세요.")
    @NotBlank(message = "회원명란은 공백일 수 없습니다.")
    @Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이내로 작성해주세요.")
    private String nickname;
}
