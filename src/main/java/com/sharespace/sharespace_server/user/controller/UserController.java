package com.sharespace.sharespace_server.user.controller;

import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.user.dto.UserEmailValidateRequest;
import com.sharespace.sharespace_server.user.dto.UserRegisterRequest;
import com.sharespace.sharespace_server.user.dto.UserUpdateRequest;
import com.sharespace.sharespace_server.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> register(@Valid @RequestBody UserRegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/validate")
    public BaseResponse<Void> emailValidate(@Valid @RequestBody UserEmailValidateRequest request) {
        return userService.emailValidate(request);
    }

    @PutMapping("/update")
    public BaseResponse<Void> update(@Valid @ModelAttribute UserUpdateRequest request) {
        return userService.update(request);
    }
}
