package com.sharespace.sharespace_server.product.entity;

import com.sharespace.sharespace_server.global.enums.Category;
import com.sharespace.sharespace_server.global.exception.CustomRuntimeException;
import com.sharespace.sharespace_server.global.exception.error.MatchingException;
import com.sharespace.sharespace_server.place.entity.Place;
import com.sharespace.sharespace_server.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "product", schema = "sharespace")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 외래 키로 User와의 관계 설정

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 10)
    private Category category;

    @Column(name = "period", nullable = false)
    private int period;

    @Column(name = "description", length = 100)
    private String description;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "writed_at", nullable = false)
    private LocalDateTime writedAt;

    @Column(name = "is_placed", nullable = false)
    private Boolean isPlaced;

    // 물품 배정 상태를 false로 set하는 상태 변경 함수
    public void unassign() {
        this.setIsPlaced(false);
    }

    // Product와 Place의 카테고리 검증 메서드
    public void validateCategoryForPlace(Place place) {
        if (this.getCategory().getValue() > place.getCategory().getValue()) {
            throw new CustomRuntimeException(MatchingException.CATEGORY_NOT_MATCHED);
        }
    }

    // Product와 Place의 기간(Period) 검증 메서드
    public void validatePeriodForPlace(Place place) {
        if (this.getPeriod() > place.getPeriod()) {
            throw new CustomRuntimeException(MatchingException.INVALID_PRODUCT_PERIOD);
        }
    }
}
