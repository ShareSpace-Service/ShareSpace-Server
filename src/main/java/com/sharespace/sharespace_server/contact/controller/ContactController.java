package com.sharespace.sharespace_server.contact.controller;

import com.sharespace.sharespace_server.contact.dto.ContactAppealRequest;
import com.sharespace.sharespace_server.contact.service.ContactService;
import com.sharespace.sharespace_server.global.annotation.CheckPermission;
import com.sharespace.sharespace_server.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/contact")
@RequiredArgsConstructor
public class ContactController {
    private final ContactService contactService;

    @PostMapping
    @CheckPermission(roles = {"ROLE_GUEST", "ROLE_HOST"})
    public BaseResponse<Void> appeal(@RequestBody ContactAppealRequest request) {
        return contactService.appeal(request);
    }
}
