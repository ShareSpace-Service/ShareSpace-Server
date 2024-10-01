package com.sharespace.sharespace_server.place.entity;

import com.sharespace.sharespace_server.global.enums.Category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

	// @ManyToOne(fetch = FetchType.LAZY)
	// @JoinColumn(name = "user_id", nullable = false)
	// private User user;

	@Column(name = "title", nullable = false, length = 50)
	private String title;

	@Enumerated(EnumType.STRING)
	private Category category;

	@Column(name = "period", nullable = false)
	private Integer period;

	@Column(name = "description", length = 100)
	private String description;

	@Column(name = "image_url", nullable = false)
	private String imageUrl;
}
