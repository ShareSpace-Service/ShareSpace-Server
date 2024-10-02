package com.sharespace.sharespace_server.product.entity;

import com.sharespace.sharespace_server.global.enums.Category;
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

    @Column(name = "image_url", columnDefinition = "TEXT", nullable = false)
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "writed_at", nullable = false)
    private LocalDateTime writedAt;

    @Column(name = "is_placed", nullable = false)
    private Boolean isPlaced;
}
