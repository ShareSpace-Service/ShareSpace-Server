package com.sharespace.sharespace_server.contact.service;

import com.sharespace.sharespace_server.contact.dto.ContactAppealRequest;
import com.sharespace.sharespace_server.contact.entity.Contact;
import com.sharespace.sharespace_server.contact.repository.ContactRepository;
import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.global.response.BaseResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepository contactRepository;
    private final BaseResponseService baseResponseService;

    @Transactional
    public BaseResponse<Void> appeal(ContactAppealRequest request) {
        Contact contact = Contact.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        contactRepository.save(contact);

        return baseResponseService.getSuccessResponse();
    }
}
