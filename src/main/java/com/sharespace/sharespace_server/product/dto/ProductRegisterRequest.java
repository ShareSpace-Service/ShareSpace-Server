package com.sharespace.sharespace_server.product.dto;

import com.sharespace.sharespace_server.global.enums.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductRegisterRequest {

    private String title;

    private Category category;

    private Integer period;

    private String imageUrl;

    private String description;

}
