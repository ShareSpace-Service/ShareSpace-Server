package com.sharespace.sharespace_server.notification.entity;

import java.time.LocalDateTime;

import com.sharespace.sharespace_server.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Notification {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	private boolean isRead;
	@Column(columnDefinition = "TEXT")
	private String message;
	private LocalDateTime createdAt;

	public static Notification create(User user, String message) {
		Notification notification = new Notification();
		notification.setUser(user);
		notification.setMessage(message);
		notification.setCreatedAt(LocalDateTime.now());
		notification.setRead(false);
		return notification;
	}
}
