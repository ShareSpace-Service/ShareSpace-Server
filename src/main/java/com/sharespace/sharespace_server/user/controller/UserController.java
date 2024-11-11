package com.sharespace.sharespace_server.user.controller;

import com.sharespace.sharespace_server.global.annotation.CheckPermission;
import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.global.utils.RequestParser;
import com.sharespace.sharespace_server.user.dto.UserEmailValidateRequest;
import com.sharespace.sharespace_server.user.dto.UserGetIdResponse;
import com.sharespace.sharespace_server.user.dto.UserGetInfoResponse;
import com.sharespace.sharespace_server.user.dto.UserRegisterRequest;
import com.sharespace.sharespace_server.user.dto.UserRegisterResponse;
import com.sharespace.sharespace_server.user.dto.UserUpdateRequest;
import com.sharespace.sharespace_server.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // task: 회원가입
    @PostMapping("/register")
    public BaseResponse<UserRegisterResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        return userService.register(request);
    }

    // task: 이메일 인증
    @PostMapping("/validate")
    public BaseResponse<Void> emailValidate(@Valid @RequestBody UserEmailValidateRequest request) {
        return userService.emailValidate(request);
    }

    // task: 유저 정보 수정
    @PutMapping("/update")
    @CheckPermission(roles = {"ROLE_GUEST", "ROLE_HOST"})
    public BaseResponse<Void> update(@Valid @ModelAttribute UserUpdateRequest request, HttpServletRequest httpRequest) {
        Long userId = RequestParser.extractUserId(httpRequest);
        return userService.update(request, userId);
    }

    // task: 로그인 유저 주소 가져오기
    @GetMapping("/place")
    @CheckPermission(roles = {"ROLE_GUEST", "ROLE_HOST"})
    public BaseResponse<String> getPlace(HttpServletRequest request) {
        Long userId = RequestParser.extractUserId(request);
        return userService.getPlace(userId);
    }

    // task: 로그인 유저 정보 가져오기
    @GetMapping("/detail")
    @CheckPermission(roles = {"ROLE_GUEST", "ROLE_HOST"})
    public BaseResponse<UserGetInfoResponse> getInfo(HttpServletRequest request) {
        Long userId = RequestParser.extractUserId(request);
        return userService.getInfo(userId);
    }

    // task: 로그아웃
    @PostMapping("/logout")
    @CheckPermission(roles = {"ROLE_GUEST", "ROLE_HOST"})
    public BaseResponse<Void> logout(@CookieValue("accessToken") String accessToken,
                                     HttpServletResponse response, HttpServletRequest request) {
        Long userId = RequestParser.extractUserId(request);
        return userService.logout(accessToken, response, userId);
    }

    // 로그인 여부 확인
    @GetMapping("/checkLogin")
    @CheckPermission(roles = {"ROLE_GUEST", "ROLE_HOST"})
    public BaseResponse<Void> checkLogin() {
        return userService.checkLogin();
    }

    // 로그인한 유저 ID 가져오기
    @GetMapping("/userId")
    @CheckPermission(roles = {"ROLE_GUEST", "ROLE_HOST"})
    public BaseResponse<UserGetIdResponse> getUserId(HttpServletRequest request) {
        Long userId = RequestParser.extractUserId(request);
        return userService.getUserId(userId);
    }

}
