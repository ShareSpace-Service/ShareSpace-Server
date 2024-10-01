package com.sharespace.sharespace_server.note.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "note", schema = "sharespace")
@Getter
@Setter
@NoArgsConstructor
public class Note {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// @ManyToOne(fetch = FetchType.LAZY)
	// @JoinColumn(name = "user_id", nullable = false)
	// public User senderId;		// 송신자

	// @ManyToOne(fetch = FetchType.LAZY)
	// @JoinColumn(name = "user_id", nullable = false)
	// public User receiverId;		// 수신자

	@Column(name = "title", nullable = false)
	public String title;

	@Column(name = "message", nullable = false)
	public String content;

	@Column(name = "created_at", nullable = false)
	public LocalDateTime send_at;
}
