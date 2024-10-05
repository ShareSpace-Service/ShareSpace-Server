package com.sharespace.sharespace_server.product.service;

import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.global.response.BaseResponseService;
import com.sharespace.sharespace_server.product.dto.ProductRegisterRequest;
import com.sharespace.sharespace_server.product.entity.Product;
import com.sharespace.sharespace_server.product.repository.ProductRepository;
import com.sharespace.sharespace_server.user.entity.User;
import com.sharespace.sharespace_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final BaseResponseService baseResponseService;

    @Transactional
    public BaseResponse<Void> register(ProductRegisterRequest request, Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        Product product = Product.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .period(request.getPeriod())
                .imageUrl(request.getImageUrl())
                .description(request.getDescription())
                .user(user)
                .isPlaced(false)
                .build();

        productRepository.save(product);

        return baseResponseService.getSuccessResponse();
    }
}
