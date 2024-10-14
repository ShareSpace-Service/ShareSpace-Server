package com.sharespace.sharespace_server.product.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.sharespace.sharespace_server.global.enums.Category;

import jakarta.validation.constraints.Min;
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
public class ProductRegisterRequest {
    @NotNull
    @NotEmpty
    private String title;
    @NotNull
    private Category category;
    @NotNull
    @Min(1)
    private Integer period;
    @NotNull
    @NotEmpty
    private String imageUrl;
    private String description;

}
