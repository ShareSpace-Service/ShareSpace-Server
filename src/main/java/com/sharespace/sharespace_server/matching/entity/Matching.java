package com.sharespace.sharespace_server.matching.entity;

import java.time.LocalDateTime;

import com.sharespace.sharespace_server.global.enums.Status;
import com.sharespace.sharespace_server.place.entity.Place;
import com.sharespace.sharespace_server.product.entity.Product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "matching")
public class Matching {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// productId와 placeId 연관관계 매핑하기

	@ManyToOne
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@ManyToOne
	@JoinColumn(name = "place_id", nullable = false)
	private Place place;


	@Column(columnDefinition = "TEXT")
	private String image;

	@Enumerated(EnumType.STRING)
	private Status status;

	private boolean hostCompleted;

	private boolean guestCompleted;

	private Integer distance;

	private LocalDateTime startDate;

}
