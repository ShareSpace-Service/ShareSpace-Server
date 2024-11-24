package com.sharespace.sharespace_server.product.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.sharespace.sharespace_server.global.enums.Category;

import jakarta.validation.constraints.Min;
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
public class ProductRegisterRequest {
    @NotNull(message = "제목을 필수로 입력해주세요")
    @NotEmpty
    @Size(max = 50, message = "제목은 50자 이내로 작성해주세요.")
    private String title;
    @NotNull(message = "카테고리를 필수로 입력해주세요")
    private Category category;
    @NotNull(message = "기간을 필수로 입력해주세요")
    @Min(1)
    private Integer period;
    @NotNull(message = "사진을 필수로 입력해주세요")
    @NotEmpty
    private List<MultipartFile> imageUrl;
    @Size(max = 100, message = "요청사항은 100자 이내로 작성해주세요")
    private String description;

}
