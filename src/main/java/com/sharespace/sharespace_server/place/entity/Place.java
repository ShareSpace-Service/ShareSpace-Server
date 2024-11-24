package com.sharespace.sharespace_server.place.entity;

import java.util.List;
import java.util.Objects;

import com.sharespace.sharespace_server.global.enums.Category;
import com.sharespace.sharespace_server.place.dto.request.PlaceRequest;
import com.sharespace.sharespace_server.place.dto.request.PlaceUpdateRequest;
import com.sharespace.sharespace_server.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "place", schema = "sharespace")
@Getter
@Setter
@NoArgsConstructor
public class Place {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "title", nullable = false, length = 50)
	private String title;

	@Enumerated(EnumType.STRING)
	@Column(name = "category", nullable = false)
	private Category category;

	@Column(name = "period", nullable = false)
	private Integer period;

	@Column(name = "description", length = 100)
	private String description;

	@Column(name = "image_url", columnDefinition = "TEXT", nullable = false)
	private String imageUrl;

	@Builder
	public Place(User user, String title, Category category, Integer period, String description, String imageUrl) {
		this.user = user;
		this.title = title;
		this.category = category;
		this.period = period;
		this.description = description;
		this.imageUrl = imageUrl;
	}

	public static Place of(PlaceRequest placeRequest, User user, List<String> combinedImageUrls) {
		return Place.builder()
			.user(user)
			.title(placeRequest.getTitle())
			.category(placeRequest.getCategory())
			.period(placeRequest.getPeriod())
			.description(placeRequest.getDescription())
			.imageUrl(String.join(",", combinedImageUrls))
			.build();
	}

	public void updateFields(PlaceUpdateRequest placeRequest, List<String> updatedImages) {
		Integer period = Integer.parseInt(placeRequest.getPeriod());
		if (!Objects.equals(this.title, placeRequest.getTitle())) {
			this.title = placeRequest.getTitle();
		}
		if (!Objects.equals(this.period, period)) {
			this.period = period;
		}
		if (!Objects.equals(this.category, placeRequest.getCategory())) {
			this.category = placeRequest.getCategory();
		}
		if (!Objects.equals(this.description, placeRequest.getDescription())) {
			this.description = placeRequest.getDescription();
		}
		this.imageUrl = String.join(",", updatedImages);
	}
}
