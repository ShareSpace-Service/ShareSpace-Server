package com.sharespace.sharespace_server.matching.entity;

import java.time.LocalDateTime;

import com.sharespace.sharespace_server.global.enums.Status;
import com.sharespace.sharespace_server.global.exception.CustomRuntimeException;
import com.sharespace.sharespace_server.global.exception.error.MatchingException;
import com.sharespace.sharespace_server.global.utils.LocationTransform;
import com.sharespace.sharespace_server.place.entity.Place;
import com.sharespace.sharespace_server.product.entity.Product;
import com.sharespace.sharespace_server.user.entity.User;

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

	public static Matching create(Product product, Place place) {
		Matching matching = new Matching();
		matching.setProduct(product);
		matching.setPlace(place);
		matching.setStatus(Status.REQUESTED);
		matching.setStartDate(LocalDateTime.now());

		Integer distance = calculateDistance(product.getUser(), place.getUser());
		matching.setDistance(distance);

		product.setIsPlaced(true);

		return matching;
	}

	private static Integer calculateDistance(User guest, User host) {
		return LocationTransform.calculateDistance(guest.getLatitude(), guest.getLongitude(), host.getLatitude(), host.getLongitude());
	}

	public void completeStorage(User user) {
		if (user.getRole().getValue().equals("GUEST")) {
			completeGuestStorage();
		} else if (user.getRole().getValue().equals("HOST")) {
			completeHostStorage();
		}

		if (this.isGuestCompleted() && this.isHostCompleted()) {
			this.setStatus(Status.COMPLETED);
		}
	}

	private void completeGuestStorage() {
		if (this.isGuestCompleted()) {
			throw new CustomRuntimeException(MatchingException.GUEST_ALREADY_COMPLETED_KEEPING);
		}
		this.setGuestCompleted(true);
	}

	private void completeHostStorage() {
		if (this.isHostCompleted()) {
			throw new CustomRuntimeException(MatchingException.HOST_ALREADY_COMPLETED_KEEPING);
		}
		this.setHostCompleted(true);
	}

	public void cancel(User user) {
		if (!this.getStatus().equals(Status.PENDING)) {
			throw new CustomRuntimeException(MatchingException.REQUEST_CANCELLATION_NOT_ALLOWED);
		}

		// 물품 배정 상태를 변경
		this.product.unassign();

		// 유저 역할에 따른 상태 변경
		if (user.getRole().getValue().equals("GUEST")) {
			this.setStatus(Status.UNASSIGNED);  // GUEST가 취소할 경우
		} else if (user.getRole().getValue().equals("HOST")) {
			this.setStatus(Status.REJECTED);  // HOST가 취소할 경우
		}
	}

	public void confirmStorageByGuest() {
		if (!this.getStatus().equals(Status.PENDING)) {
			throw new CustomRuntimeException(MatchingException.INCORRECT_STATUS_CONFIRM_REQUEST_GUEST);
		}
		this.setStatus(Status.STORED);
	}
}
