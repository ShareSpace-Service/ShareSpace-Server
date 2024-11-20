package com.sharespace.sharespace_server.contact.dto;

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
public class ContactAppealRequest {
    @NotNull(message = "제목을 입력해주세요")
    @Size(max = 50, message = "제목은 50자 이내로 작성해주세요")
    private String title;

    @NotNull(message = "내용을 입력해주세요")
    @Size(max = 200, message = "내용은 200자 이내로 작성해주세요")
    private String content;
}
