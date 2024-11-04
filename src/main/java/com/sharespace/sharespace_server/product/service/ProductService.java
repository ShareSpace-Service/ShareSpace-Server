package com.sharespace.sharespace_server.product.service;

import java.util.List;

import com.sharespace.sharespace_server.global.exception.CustomRuntimeException;
import com.sharespace.sharespace_server.global.exception.error.ImageException;
import com.sharespace.sharespace_server.global.response.BaseResponse;
import com.sharespace.sharespace_server.global.response.BaseResponseService;
import com.sharespace.sharespace_server.global.utils.S3ImageUpload;
import com.sharespace.sharespace_server.matching.entity.Matching;
import com.sharespace.sharespace_server.matching.repository.MatchingRepository;
import com.sharespace.sharespace_server.product.dto.ProductRegisterRequest;
import com.sharespace.sharespace_server.product.dto.ProductRegisterResponse;
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
    private final S3ImageUpload s3ImageUpload;
    private final MatchingRepository matchingRepository;

    @Transactional
    public BaseResponse<ProductRegisterResponse> register(ProductRegisterRequest request, Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        if (!s3ImageUpload.hasValidImages(request.getImageUrl())) {
            throw new CustomRuntimeException(ImageException.IMAGE_REQUIRED_FIELDS_EMPTY);
        }

        Product product = Product.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .period(request.getPeriod())
                .description(request.getDescription())
                .user(user)
                .isPlaced(false)
                .build();
        productRepository.save(product);

        List<String> imagesUrl = s3ImageUpload.uploadImages(request.getImageUrl(), "product/" + product.getId());
        product.setImageUrl(String.join(",", imagesUrl));

        // 장소 등록하면 우선 Matching 테이블에 save
        Matching matching = Matching.createNotSelectedPlace(product);
        matchingRepository.save(matching);

        ProductRegisterResponse response = ProductRegisterResponse.builder()
            .matchingId(matching.getId())
            .build();

        return baseResponseService.getSuccessResponse(response);
    }
}
